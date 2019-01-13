package smartx.multiview.flowcentric

import java.io.File
import java.sql.Timestamp
import java.time.LocalDateTime

import org.apache.commons.io.FileUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.functions.{current_timestamp, from_unixtime, unix_timestamp}

object Main {
  def main(args: Array[String]) {
    //Verify Visibility Center IP is provided.
    if (args.length == 0) {
      System.out.println("Please provide Visibility Center IP as an argument.")
      System.exit(1)
    }

    val VISIBILITY_CENTER_IP = args(0)

    val processing_time = current_timestamp()

    //Create a SparkContext to initialize Spark
    val spark = SparkSession.builder()
      .master("local[*]")
      .appName("Flow-centric Integration")
      .config("es.nodes", VISIBILITY_CENTER_IP)
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .config("pushdown", "true")
      .getOrCreate()
    import spark.implicits._

    IntegrateMultilayerVisibility(spark)
    spark.close()

    //Underlay Network Resource-layer Data Summarization
    def IntegrateMultilayerVisibility(spark: SparkSession): Unit = {
      //Load data from Staging
      val LatencyUnderlayDF = spark.read.load("/home/netcs/Multi-View-Staged/AggregateUndelayLatency.parquet")
      val SystemPhysicalDF = spark.read.load("/home/netcs/Multi-View-Staged/AggregateSystemPhysical.parquet")
      val SystemVirtualDF = spark.read.load("/home/netcs/Multi-View-Staged/AggregateSystemVirtual.parquet")
      var MCPlanesDF = spark.read.load("/home/netcs/Multi-View-Staged/AggregateControlPlane.parquet")
      var DPlanesDF = spark.read.load("/home/netcs/Multi-View-Staged/AggregateDataPlane.parquet")

      DPlanesDF = DPlanesDF.withColumnRenamed("processing_time" , "processingTime")

      val FirstResult = DPlanesDF.join(right = SystemPhysicalDF, col("measurementboxname") === col("BoxID"), joinType = "left_outer")
        .drop("measurementboxname")
        .drop("minipat")
        .drop("maxipat")
        .drop("avgipat")
        .drop("stddevipat")
        .drop("")

      val lookup1 = Map(
        "BoxID" -> "Box_ID",
        "cpuload5" -> "pcpuload5",
        "system" -> "psystem", "user" -> "puser", "steal" -> "psteal", "nice" -> "pnice", "iowait" -> "piowait", "idle" -> "pidle",
        "total_memory"-> "ptotal_memory", "free_memory"-> "pfree_memory", "used_memory"-> "pused_memory", "buffers"-> "pbuffers", "cached"-> "pcached",
        "total_disk"-> "ptotal_disk", "free_disk" -> "pfree_disk", "used_disk" -> "pused_disk",
        "management" -> "pmanagement", "mgmt_bytes_sent" -> "pmgmt_bytes_sent", "mgmt_bytes_recv" -> "pmgmt_bytes_recv", "mgmt_packets_sent" -> "pmgmt_packets_sent", "mgmt_packets_recv" -> "pmgmt_packets_recv",
        "control" -> "pcontrol", "ctrl_bytes_sent" -> "pctrl_bytes_sent", "ctrl_bytes_recv" -> "pctrl_bytes_recv", "ctrl_packets_sent" -> "pctrl_packets_sent", "ctrl_packets_recv" -> "pctrl_packets_recv",
        "data" -> "pdata", "data_bytes_sent" -> "pdata_bytes_sent", "data_bytes_recv" -> "pdata_bytes_recv", "data_packets_sent" -> "pdata_packets_sent", "data_packets_recv" -> "pdata_packets_recv"
      )

      val FirstResultRenamed = FirstResult.select(FirstResult.columns.map(c => col(c).as(lookup1.getOrElse(c, c))): _*)

      val SecondResult = FirstResultRenamed.join(right = SystemVirtualDF, col("vmid") === col("BoxID"), joinType = "left_outer")
          .drop("BoxID").drop("processing_time")

      val lookup2 = Map(
        "load5" -> "vcpuload5",
        "system" -> "vsystem", "user" -> "vuser", "steal" -> "vsteal", "nice" -> "vnice", "iowait" -> "viowait", "idle" -> "vidle",
        "total_memory"-> "vtotal_memory", "free_memory"-> "vfree_memory", "used_memory"-> "vused_memory", "buffers"-> "vbuffers", "cached"-> "vcached",
        "total_disk"-> "vtotal_disk", "free_disk" -> "vfree_disk", "used_disk" -> "vused_disk",
        "management" -> "vmanagement", "mgmt_bytes_sent" -> "vmgmt_bytes_sent", "mgmt_bytes_recv" -> "vmgmt_bytes_recv", "mgmt_packets_sent" -> "vmgmt_packets_sent", "mgmt_packets_recv" -> "vmgmt_packets_recv",
        "control" -> "vcontrol", "ctrl_bytes_sent" -> "vctrl_bytes_sent", "ctrl_bytes_recv" -> "vctrl_bytes_recv", "ctrl_packets_sent" -> "vctrl_packets_sent", "ctrl_packets_recv" -> "vctrl_packets_recv",
        "data" -> "vdata", "data_bytes_sent" -> "vdata_bytes_sent", "data_bytes_recv" -> "vdata_bytes_recv", "data_packets_sent" -> "vdata_packets_sent", "data_packets_recv" -> "vdata_packets_recv"
      )

      val SecondResultRenamed = SecondResult.select(SecondResult.columns.map(c => col(c).as(lookup2.getOrElse(c, c))): _*)

      val ThirdResult = SecondResultRenamed.join(right = LatencyUnderlayDF, col("Box_ID") === col("SmartX-Box-Source"), joinType = "left_outer")
        .drop("SmartX-Box-Source").drop("processing_time")

      System.out.println("Data Plane Records: "+DPlanesDF.count())
      System.out.println("Control Plane Records: "+MCPlanesDF.count())
      System.out.println("Underlay Latency Records: "+LatencyUnderlayDF.count())
      System.out.println("Physical System Records: "+SystemPhysicalDF.count())
      System.out.println("Virtual System Records: "+SystemVirtualDF.count())
      System.out.println("DP + Physical Records: "+FirstResultRenamed.count())
      System.out.println("DP + Virtual Records: "+SecondResultRenamed.count())
      System.out.println("DP + Underlay Records: "+ThirdResult.count())

      ThirdResult.show(50, false)

      //Remove previous data files from the disk
      val dir = new File("/home/netcs/Multi-View-Integrated/")
      FileUtils.deleteDirectory(dir)

      //Save result to parquet file format
      ThirdResult.toDF.write.parquet("/home/netcs/Multi-View-Integrated/data.parquet")

      // Stage data to ElasticSearch DataStore
      ThirdResult.write.mode("append")
        .format("org.elasticsearch.spark.sql")
        .option("es.mapping.date.rich", "false")
        .save("dp-integrated-flows/data-integrated")
    }
  }
}
