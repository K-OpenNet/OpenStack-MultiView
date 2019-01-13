package smartx.multiview.flowcentric

import java.io.File
import java.sql.Timestamp
import java.time.LocalDateTime

import com.mongodb.spark._
import com.mongodb.spark.config._
import com.mongodb.spark.sql._
import org.apache.commons.io.FileUtils
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.elasticsearch.spark.sql._

object Main {

  def main(args: Array[String]) {
    //Verify Visibility Center IP is provided.
    if (args.length == 0) {
      System.out.println("Please provide Visibility Center IP as an argument.")
      System.exit(1)
    }

    val VISIBILITY_CENTER_IP = args(0)

    val end_time = LocalDateTime.now().minusMinutes(1)
    val start_time = LocalDateTime.now().minusMinutes(6)
    val processing_time = current_timestamp()

    //Create a SparkContext to initialize Spark
    val spark = SparkSession.builder()
      .master("local[*]")
      .appName("Flow-centric Validation Tagging Aggregation")
      .config("es.nodes", VISIBILITY_CENTER_IP)
      .config("es.port", "9200")
      .config("es.index.auto.create", "true")
      .config("pushdown", "true")
      .getOrCreate()
    import spark.implicits._

    //Read latest packets file from directory
    def getListOfFiles(dir: String): List[File] = {
      val d = new File(dir)
      if (d.exists.&&(d.isDirectory)) {
        d.listFiles.filter(_.isFile).toList
      } else {
        scala.List[File]()
      }
    }

    //Remove previous data files from the disk
    val dir = new File("/home/netcs/Multi-View-Staged/")
    FileUtils.deleteDirectory(dir)

    AggregateLatencyUnderlay(spark)
    AggregateSystemPhysical(spark)
    AggregateSystemVirtual(spark)
    AggregateDataPlane(spark)
    AggregateControlPlane(spark)
    spark.close()

    //Underlay Network Resource-layer Data Summarization
    def AggregateLatencyUnderlay(spark: SparkSession): Unit = {
      //Query last 10 minutes data
      val start_time_a = start_time.minusMinutes(5)

      //Load data from MongoDB
      val readConfig1 = ReadConfig(Map("uri" -> "mongodb://127.0.0.1/", "database" -> "multiviewdb", "collection" -> "daily-report-latency-data-raw_extended"))

      //Query and filter data
      val selectResult = spark.read.mongo(readConfig1)
        .filter(from_unixtime(unix_timestamp($"timestamp", "yyyy/MM/dd HH:mm:ss")).between(Timestamp.valueOf(start_time_a), Timestamp.valueOf(end_time)))
        .select("SmartX-Box-Source", "SmartX-Box-GIST1", "SmartX-Box-GIST2", "SmartX-Box-GIST3", "SmartX-Box-CHULA", "SmartX-Box-HUST", "SmartX-Box-MYREN", "SmartX-Box-PH", "SmartX-Box-NCKU", "SmartX-Box-MY")

      //Group result
      val groupResult = selectResult.groupBy("SmartX-Box-Source")
        .agg(
          round(avg("SmartX-Box-CHULA"), 2).as("SmartX-Box-CHULA"),
          round(avg("SmartX-Box-GIST1"), 2).as("SmartX-Box-GIST1"),
          round(avg("SmartX-Box-GIST2"), 2).as("SmartX-Box-GIST2"),
          round(avg("SmartX-Box-GIST3"), 2).as("SmartX-Box-GIST3"),
          round(avg("SmartX-Box-HUST"), 2).as("SmartX-Box-HUST"),
          round(avg("SmartX-Box-MYREN"), 2).as("SmartX-Box-MYREN"),
          round(avg("SmartX-Box-PH"), 2).as("SmartX-Box-PH"),
          round(avg("SmartX-Box-MY"), 2).as("OFTEIN-MY-UM"),
          round(avg("SmartX-Box-NCKU"), 2).as("SmartX-Box-NCKU")
        )

      //Add processing time column to data frame
      val FinalResult = groupResult.withColumn("processing_time", processing_time)

      //Save result to parquet file format
      FinalResult.toDF.write.parquet("/home/netcs/Multi-View-Staged/AggregateUndelayLatency.parquet")

      //Save result to Elasticsearch
      FinalResult.write.mode("append").format("org.elasticsearch.spark.sql").option("es.mapping.date.rich", "false").save("underlay-staged-latency/latency")
    }

    //Physical Resource-layer Data Summarization
    def AggregateSystemPhysical(spark: SparkSession): Unit = {
      val df = spark.esDF("smartx-boxes-metrics/instance").filter(unix_timestamp($"@timestamp", "yyyy-MM-ddTHH:mm:ss.S").cast("timestamp").between(Timestamp.valueOf(start_time), Timestamp.valueOf(end_time)))

      val groupResult = df.groupBy("BoxID")
        .agg(
          round(avg("load5"),4) as "cpuload5",
          round(avg("system"),2) as "system",
          round(avg("user"),2) as "user",
          round(avg("steal"),2) as "steal",
          round(avg("nice"),2) as "nice",
          round(avg("iowait"),2) as "iowait",
          round(avg("idle"),2) as "idle",
          round(avg("mem_total")*0.000001,0) as "total_memory",
          round(avg("mem_free")*0.000001,2) as "free_memory",
          round(avg("mem_used")*0.000001,2) as "used_memory",
          round(avg("buffers")*0.000001,2) as "buffers",
          round(avg("cached")*0.000001,2) as "cached",
          round(avg("space_total")*0.001,0) as "total_disk",
          round(avg("space_free")*0.001,2) as "free_disk",
          round(avg("space_used")*0.001,2) as "used_disk",
          round(avg("mgmt_bytes_sent"),2) as "mgmt_bytes_sent",
          round(avg("mgmt_bytes_recv"),2) as "mgmt_bytes_recv",
          round(avg("mgmt_packets_sent"),0) as "mgmt_packets_sent",
          round(avg("mgmt_packets_recv"),0) as "mgmt_packets_recv",
          round(avg("ctrl_bytes_sent"),2) as "ctrl_bytes_sent",
          round(avg("ctrl_bytes_recv"),2) as "ctrl_bytes_recv",
          round(avg("ctrl_packets_sent"),0) as "ctrl_packets_sent",
          round(avg("ctrl_packets_recv"),0) as "ctrl_packets_recv",
          round(avg("data_bytes_sent"),2) as "data_bytes_sent",
          round(avg("data_bytes_recv"),2) as "data_bytes_recv",
          round(avg("data_packets_sent"),0) as "data_packets_sent",
          round(avg("data_packets_recv"),0) as "data_packets_recv"
        )

      //Load Physical resources data from MongoDB
      val readConfig1 = ReadConfig(Map("uri" -> "mongodb://127.0.0.1/", "database" -> "multiviewdb", "collection" -> "pbox-list"))
      val readConfig2 = ReadConfig(Map("uri" -> "mongodb://127.0.0.1/", "database" -> "multiviewdb", "collection" -> "pbox-nic-tags"))
      val readConfig3 = ReadConfig(Map("uri" -> "mongodb://127.0.0.1/", "database" -> "multiviewdb", "collection" -> "playground_sites"))
      val readConfig4 = ReadConfig(Map("uri" -> "mongodb://127.0.0.1/", "database" -> "multiviewdb", "collection" -> "underlay_REN"))

      //val customRdd = MongoSpark.load(spark, readConfig)
      val PhysicalBoxesList = spark.read.mongo(readConfig1).select("boxName", "boxType", "site")
      val NICTagsList = spark.read.mongo(readConfig2).select("boxName", "management", "control", "data")
      val SitesList = spark.read.mongo(readConfig3).select("siteID", "RENID", "name")
      val RENList = spark.read.mongo(readConfig4).select("RENID", "intID", "name")

      //Add Site Names of SmartX Boxes
      val BoxesSystemDF = groupResult.join(right = PhysicalBoxesList, col("BoxID") === col("boxName"), joinType = "left_outer")
        .select(
          "BoxID", "boxType", "site",
          "cpuload5", "system", "user", "steal", "nice", "iowait", "idle",
          "total_memory", "free_memory", "used_memory", "buffers", "cached",
          "total_disk", "free_disk", "used_disk",
          "mgmt_bytes_sent", "mgmt_bytes_recv", "mgmt_packets_sent", "mgmt_packets_recv",
          "ctrl_bytes_sent", "ctrl_bytes_recv", "ctrl_packets_sent", "ctrl_packets_recv",
          "data_bytes_sent", "data_bytes_recv", "data_packets_sent", "data_packets_recv"
        )

      //Add SmartX Boxes interface Names
      val BoxesSystemNICDF = BoxesSystemDF.join(right = NICTagsList, col("BoxID") === col("boxName"), joinType = "left_outer")
        .select(
          "cpuload5", "BoxID", "boxType", "site",
          "system", "user", "steal", "nice", "iowait", "idle",
          "total_memory", "free_memory", "used_memory", "buffers", "cached",
          "total_disk", "free_disk", "used_disk",
          "management", "mgmt_bytes_sent", "mgmt_bytes_recv", "mgmt_packets_sent", "mgmt_packets_recv",
          "control", "ctrl_bytes_sent", "ctrl_bytes_recv", "ctrl_packets_sent", "ctrl_packets_recv",
          "data", "data_bytes_sent", "data_bytes_recv", "data_packets_sent", "data_packets_recv"
        )

      //Add REN Info of Sites
      var BoxesSystemNICSiteDF = BoxesSystemNICDF.join(right = SitesList, col("site") === col("SiteID"), joinType = "left_outer")
        .select(
          "cpuload5", "BoxID", "boxType", "site", "RENID",
          "system", "user", "steal", "nice", "iowait", "idle",
          "total_memory", "free_memory", "used_memory", "buffers", "cached",
          "total_disk", "free_disk", "used_disk",
          "management", "mgmt_bytes_sent", "mgmt_bytes_recv", "mgmt_packets_sent", "mgmt_packets_recv",
          "control", "ctrl_bytes_sent", "ctrl_bytes_recv", "ctrl_packets_sent", "ctrl_packets_recv",
          "data", "data_bytes_sent", "data_bytes_recv", "data_packets_sent", "data_packets_recv"
        )
      BoxesSystemNICSiteDF = BoxesSystemNICSiteDF.withColumnRenamed("RENID", "REN")

      //Add International Underlay Network
      val BoxesSystemNICSiteRENDF = BoxesSystemNICSiteDF.join(right = RENList, col("REN") === col("RENID"), joinType = "left_outer")
        .select(
          "BoxID", "boxType", "site", "RENID", "intID",
          "cpuload5", "system", "user", "steal", "nice", "iowait", "idle",
          "total_memory", "free_memory", "used_memory", "buffers", "cached",
          "total_disk", "free_disk", "used_disk",
          "management", "mgmt_bytes_sent", "mgmt_bytes_recv", "mgmt_packets_sent", "mgmt_packets_recv",
          "control", "ctrl_bytes_sent", "ctrl_bytes_recv", "ctrl_packets_sent", "ctrl_packets_recv",
          "data", "data_bytes_sent", "data_bytes_recv", "data_packets_sent", "data_packets_recv"
        )

      //Add processing time to data frame
      val FinalResult = BoxesSystemNICSiteRENDF.withColumn("processing_time", processing_time)
      FinalResult.show(false)


      //Save result to parquet file format
      FinalResult.toDF.write.parquet("/home/netcs/Multi-View-Staged/AggregateSystemPhysical.parquet")

      //Save result to Elasticsearch
      FinalResult.write.mode("append")
        .format("org.elasticsearch.spark.sql")
        .option("es.mapping.date.rich", "false")
        .save("physical-staged-system/physical")
    }

    //Virtual Resource-layer Data Summarization
    def AggregateSystemVirtual(spark: SparkSession): Unit = {
      //val df = EsSparkSQL.esDF(spark, "openstack-instance-metrics/instance", "?q=@timestamp:["+Timestamp.valueOf(start_time)+" TO "+Timestamp.valueOf(end_time)+"]")
      val df = spark.esDF("openstack-instances-metrics/instance").filter(unix_timestamp($"@timestamp", "yyyy-MM-ddTHH:mm:ss.S").cast("timestamp").between(Timestamp.valueOf(start_time), Timestamp.valueOf(end_time)))
      //        .where(unix_timestamp($"@timestamp", "yyyy-MM-ddTHH:mm:ss.S").cast("timestamp").between(Timestamp.valueOf(start_time), Timestamp.valueOf(end_time)))

      val groupResult = df.groupBy("BoxID")
        .agg(
          round(avg("load5"),4) as "load5",
          round(avg("system"),2) as "system",
          round(avg("user"),2) as "user",
          round(avg("steal"),2) as "steal",
          round(avg("nice"),2) as "nice",
          round(avg("iowait"),2) as "iowait",
          round(avg("idle"),2) as "idle",
          round(avg("mem_total")*0.000001, 0) as "total_memory",
          round(avg("mem_free")*0.000001,2) as "free_memory",
          round(avg("mem_used")*0.000001,2) as "used_memory",
          round(avg("buffers")*0.000001,2) as "buffers",
          round(avg("cached")*0.000001,2) as "cached",
          round(avg("space_total")*0.001, 0) as "total_disk",
          round(avg("space_free")*0.001,2) as "free_disk",
          round(avg("space_used")*0.001,2) as "used_disk",
          round(avg("control_bytes_sent"),2) as "ctrl_bytes_sent",
          round(avg("control_bytes_recv"),2) as "ctrl_bytes_recv",
          round(avg("control_packets_sent"),0) as "ctrl_packets_sent",
          round(avg("control_packets_recv"),0) as "ctrl_packets_recv",
          round(avg("data_bytes_sent"),2) as "data_bytes_sent",
          round(avg("data_bytes_recv"),2) as "data_bytes_recv",
          round(avg("data_packets_sent"),0) as "data_packets_sent",
          round(avg("data_packets_recv"),0) as "data_packets_recv"
        )

      //Add processing time to data frame
      val FinalResult = groupResult.withColumn("processing_time", processing_time)

      //Save result to parquet file format
      FinalResult.toDF.write.parquet("/home/netcs/Multi-View-Staged/AggregateSystemVirtual.parquet")

      //Save result to Elasticsearch
      FinalResult.write.mode("append")
        .format("org.elasticsearch.spark.sql")
        .option("es.mapping.date.rich", "false")
        .save("virtual-staged-system/virtual")
    }

    //Data Plane network packets data processing
    def AggregateDataPlane(spark: SparkSession): Unit = {
      //Load network packets data from disk. Validate network packets data according to the defined class
      val files = getListOfFiles("/home/netcs/IOVisor-Data/Data-Plane-Latest/")
      //val LatestDataFile1 = file1.maxBy(_.lastModified)

      //Create an empty data set
      var allPackets = Seq.empty[DataFrame]

      //Read all the network packets files for last 5 minutes
      for (file <- files) {
        val packetFile = spark.read.format("csv").option("header", "false").load(file.toString)
        val packets = packetFile.map(p => ValidateDataPlane.apply(p(0).toString.toDouble, p(1).toString, p(2).toString, p(3).toString.toInt, p(4).toString, p(5).toString, p(6).toString.toInt, p(7).toString.toInt, p(8).toString, p(9).toString, p(10).toString.toInt, p(11).toString.toInt, p(12).toString.toInt, p(13).toString.toInt, p(14).toString.toInt, p(15).toString.toInt, p(16).toString.toInt)).toDF()
        allPackets = allPackets :+ packets
      }
      System.out.println(allPackets)

      //Combine all data frames together
      val FinalPackets = allPackets.reduce(_.union(_))

      //Remove metadata fields
      val selectResult = FinalPackets.select("collectiontime", "measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "src_vm_ip", "dest_vm_ip", "src_vm_port", "dest_vm_port", "vlan", "protocol", "tcp_window_size", "databytes").cache()
      System.out.println("Total Packets: "+selectResult.count())

      //SmartX Tagging for tenants
      val tenanttagFile = spark.read.format("csv").option("header", "false").load("/home/netcs/SmartX-Tagging/tenant.tags")
      val tenanttags = tenanttagFile.map(p => TenantsTagClass.apply(p(0).toString, p(1).toString, p(2).toString, p(3).toString)).toDF()
      val uniquetenanttags = tenanttags.distinct()
      val TenantTaggedFlow = selectResult.join(right = uniquetenanttags, col("vlan") === col("vlanid"), joinType = "left_outer").select("collectiontime", "measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "src_vm_ip", "dest_vm_ip", "src_vm_port", "dest_vm_port", "vlan", "subnet", "protocol", "tcp_window_size", "databytes", "tenantid", "tenantname").cache
      System.out.println("TenantTaggedFlow Packets: "+TenantTaggedFlow.count())

      //SmartX Tagging for OpenStack VM Instances
      val vmtagFile = spark.read.format("csv").option("header", "false").load("/home/netcs/SmartX-Tagging/ControlPlane.tags")
      val vmtags = vmtagFile.map(p => VMTagsClass.apply(p(0).toString, p(1).toString, p(4).toString, p(6).toString)).toDF()
      val VMTaggedFlow = TenantTaggedFlow.join(right = vmtags, col("src_vm_ip") === col("dataip"), joinType = "left_outer").select("collectiontime", "measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "src_vm_ip", "dest_vm_ip", "src_vm_port", "dest_vm_port", "vlan", "subnet", "protocol", "tcp_window_size", "databytes", "tenantid", "tenantname", "vmid")
      System.out.println("VMTaggedFlow Packets: "+VMTaggedFlow.count())

      //SmartX Tagging for application tags
      val applicationtagFile = spark.read.format("csv").option("header", "false").load("/home/netcs/SmartX-Tagging/smartx.tags")
      val AppTags = applicationtagFile.map(p => AppsTagsClass.apply(p(0).toString.toInt, p(1).toString.toInt, p(2).toString.toInt, p(3).toString)).toDF()
      val AppTaggedFlow = VMTaggedFlow.join(right = AppTags, col("protocol") === col("protocolid") && (col("src_vm_port") === col("appport") || col("dest_vm_port") === col("appport")), joinType = "left_outer").select("collectiontime", "measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "src_vm_ip", "dest_vm_ip", "src_vm_port", "dest_vm_port", "vlan", "subnet", "protocol", "tcp_window_size", "databytes", "tenantid", "tenantname", "vmid", "tagids")
      System.out.println("AppTaggedFlow Packets: "+AppTaggedFlow.count())

      //Replace Tag IDs with Application Names
      val tagmappingFile = spark.read.format("csv").option("header", "false").load("/home/netcs/SmartX-Tagging/app_class.map")
      val mappingtags = tagmappingFile.map(p => TagsMappingClass.apply(p(0).toString.toInt, p(1).toString.toInt, p(2).toString, p(3).toString.toInt, p(4).toString)).toDF()
      //val taggingResultFinal = AppTaggedFlow.filter($"appclassid".isNotNull)
      val taggingResultSave = AppTaggedFlow.join(right = mappingtags, col("tagids") === col("tagid"), joinType = "left_outer").select("collectiontime", "measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "src_vm_ip", "dest_vm_ip", "src_vm_port", "dest_vm_port", "vlan", "subnet", "protocol", "tcp_window_size", "databytes", "tenantname", "vmid", "appclassname", "appname")
      System.out.println("Tagging Packets: "+taggingResultSave.count())

      //Extract additonal fields
      import org.apache.spark.sql.expressions._
      import org.apache.spark.sql.functions._
      //Calcuate inter packet arrival time
      val w = Window.partitionBy($"measurementboxname", $"src_host", $"dest_host", $"src_host_port", $"dest_host_port", $"src_vm_ip", $"dest_vm_ip", $"src_vm_port", $"dest_vm_port", $"vlan", $"subnet", $"protocol", $"tcp_window_size", $"databytes", $"tenantname", $"vmid", $"appclassname", $"appname", $"collectiontime").orderBy($"collectiontime")
      val selectResultUpdated = taggingResultSave.withColumn("interpacketarrivaltime", lag($"collectiontime", 1, 0).over(w))
      val selectResultUpdated2 = selectResultUpdated.withColumn("ipat", when($"interpacketarrivaltime" === 0.0, 0.0).otherwise($"collectiontime" - $"interpacketarrivaltime"))
      val groupResult = selectResultUpdated2.groupBy("measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "src_vm_ip", "dest_vm_ip", "src_vm_port", "dest_vm_port", "vlan", "subnet", "protocol", "tenantname", "vmid", "appclassname", "appname")
        .agg(
          count("protocol") as "packets",
          round(min("tcp_window_size"),2) as "mintcpwindowsize",
          round(max("tcp_window_size"),2) as "maxtcpwindowsize",
          round(avg("tcp_window_size"),0) as "avgtcpwindowsize",
          when(stddev("tcp_window_size") === "NaN", 0.0).otherwise(round(stddev("tcp_window_size"), 2)) as "stddevtcpwindowsize",
          round(min("databytes"),2) as "mindatabytes",
          round(max("databytes"), 2) as "maxdatabytes",
          round(avg("databytes"),2) as "avgdatabytes",
          when(stddev("databytes") === "NaN", 0.0).otherwise(round(stddev("databytes"),2)) as "stddevdatabytes",
          round(sum("databytes"),0) as "totaldatabytes",
          round(min("ipat"), 2) as "minipat",
          round(max("ipat"),2) as "maxipat", round(avg("ipat"),2) as "avgipat",
          when(stddev("ipat") === "NaN", 0.0).otherwise(round(stddev("ipat"),2)) as "stddevipat",
          round((max("collectiontime") - min("collectiontime"))/1000000, 3) as "flowduration"
        )

      //Add processing time column to data frame
      val result = groupResult.withColumn("processing_time", processing_time).cache()
      System.out.println("Resultant Packets: "+result.count())

      //Save result to parquet file format
      result.toDF.write.parquet("/home/netcs/Multi-View-Staged/AggregateDataPlane.parquet")

      // Stage data to ElasticSearch DataStore
      result.write.mode("append")
        .format("org.elasticsearch.spark.sql")
        .option("es.mapping.date.rich", "false")
        .save("dp-staged-flows/summarized")
/*
      //val notTagged = AppTaggedFlow.filter("appclassid is null")
*/
      //Remove raw data file from disk
      for (file <- files) {
        Predef.println("Deleted Raw File: " + file.toString)

        FileUtils.deleteQuietly(new File(file.toString))
      }
    }

    //Control Plane network packets data processing
    def AggregateControlPlane(spark: SparkSession): Unit = {
      //Load network packets data from disk. Validate network packets data according to the defined structure
      val files = getListOfFiles("/home/netcs/IOVisor-Data/Control-Plane-Latest/")

      //Create an empty data set
      var allPackets = Seq.empty[DataFrame]

      //Read all the network packets files for last 5 minutes
      for (file <- files) {
        val packetFile = spark.read.format("csv").option("header", "false").load(file.toString)
        val packets = packetFile.map(p => ValidateControlPlane.apply(p(0).toString.toDouble, p(1).toString, p(2).toString, p(3).toString, p(4).toString.toInt, p(5).toString, p(6).toString, p(7).toString.toInt, p(8).toString.toInt, p(9).toString.toInt, p(10).toString.toInt, p(11).toString.toInt)).toDF()
        allPackets = allPackets :+ packets
      }

      //Combine all data frames together
      var FinalPackets = allPackets.reduce(_.union(_))

      //Remove unnecessary fields
      val selectResult = FinalPackets.select("collectiontime", "measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "protocol", "tcp_window_size", "databytes", "net_plane").cache()
      System.out.println("Total Packets: "+selectResult.count())

      //SmartX Tagging for tenant tags
      val tenanttagFile = spark.read.format("csv").option("header", "false").load("/home/netcs/SmartX-Tagging/ControlPlane.tags")
      val tenanttags = tenanttagFile.map(p => ControlTagsClass.apply(p(0).toString, p(1).toString, p(2).toString, p(4).toString, p(5).toString)).toDF()
      val uniquetenanttags = tenanttags.distinct()
      val TenantTaggedFlow = selectResult.join(right = uniquetenanttags, col("src_host") === col("controlip"), joinType = "left_outer").select("collectiontime", "measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "protocol", "tcp_window_size", "databytes", "net_plane", "tenantid", "tenantname", "vmid").cache

      //TenantTaggedFlow.filter("measurementboxname like 'SmartX-Box-GIST1'").show(1000, false)

      //SmartX Tagging for application tags
      val applicationtagFile = spark.read.format("csv").option("header", "false").load("/home/netcs/SmartX-Tagging/smartx.tags")
      val AppTags = applicationtagFile.map(p => AppsTagsClass.apply(p(0).toString.toInt, p(1).toString.toInt, p(2).toString.toInt, p(3).toString)).toDF()
      val AppTaggedFlow = TenantTaggedFlow.join(right = AppTags, col("protocol") === col("protocolid") && col("tenantid") === col("tenantids") && (col("src_host_port") === col("appport") || col("dest_host_port") === col("appport")), joinType = "left_outer").select("collectiontime", "measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "protocol", "tcp_window_size", "databytes", "net_plane", "tenantid", "tenantname", "vmid", "tagids")

      //Replace Tag IDs with Names
      val tagmappingFile = spark.read.format("csv").option("header", "false").load("/home/netcs/SmartX-Tagging/app_class.map")
      val mappingtags = tagmappingFile.map(p => TagsMappingClass.apply(p(0).toString.toInt, p(1).toString.toInt, p(2).toString, p(3).toString.toInt, p(4).toString)).toDF()
      //val taggingResultFinal = AppTaggedFlow.filter($"tagids".isNotNull)
      val taggingResultSave = AppTaggedFlow.join(right = mappingtags, col("tagids") === col("tagid"), joinType = "left_outer").select("collectiontime", "measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "protocol", "tcp_window_size", "databytes", "net_plane", "tenantid", "tenantname", "vmid", "appclassname", "appname")
      //val taggingResultSave = taggingResultFinal.join(right = mappingtags, col("appclassid") === col("appclassids") && col("appid") === col("appids"), joinType = "left_outer").select("@timestamp", "measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "src_vm_ip", "dest_vm_ip", "src_vm_port", "dest_vm_port", "vlan", "protocol", "packets", "mintcpwindowsize", "maxtcpwindowsize", "avgtcpwindowsize", "stddevtcpwindowsize", "mindatabytes", "maxdatabytes", "avgdatabytes", "stddevdatabytes", "totaldatabytes", "minipat", "maxipat", "avgipat", "stddevipat", "flowduration", "subnet", "tenantid", "tenantname", "appclassname", "appname")

      //Extract additonal fields
      import org.apache.spark.sql.expressions._
      import org.apache.spark.sql.functions._
      //Calcuate inter packet arrival time
      val w = Window.partitionBy($"measurementboxname", $"src_host", $"dest_host", $"src_host_port", $"dest_host_port", $"protocol", $"tcp_window_size", $"databytes", $"net_plane", $"tenantid", $"tenantname", $"vmid", $"appclassname", $"appname", $"collectiontime").orderBy($"collectiontime")
      val selectResultUpdated = taggingResultSave.withColumn("interpacketarrivaltime", lag($"collectiontime", 1, 0).over(w))
      val selectResultUpdated2 = selectResultUpdated.withColumn("ipat", when($"interpacketarrivaltime" === 0.0, 0.0).otherwise($"collectiontime" - $"interpacketarrivaltime"))
      val groupResult = selectResultUpdated2.groupBy("measurementboxname", "src_host", "dest_host", "src_host_port", "dest_host_port", "protocol", "net_plane", "tenantid", "tenantname", "vmid", "appclassname", "appname")
        .agg(
          count("protocol") as "packets",
          round(min("tcp_window_size"),2) as "mintcpwindowsize",
          round(max("tcp_window_size"),2) as "maxtcpwindowsize",
          round(avg("tcp_window_size"),0) as "avgtcpwindowsize",
          when(stddev("tcp_window_size") === "NaN", 0.0).otherwise(round(stddev("tcp_window_size"), 2)) as "stddevtcpwindowsize",
          round(min("databytes"),2) as "mindatabytes",
          round(max("databytes"), 2) as "maxdatabytes",
          round(avg("databytes"),2) as "avgdatabytes",
          when(stddev("databytes") === "NaN", 0.0).otherwise(round(stddev("databytes"),2)) as "stddevdatabytes",
          round(sum("databytes"),0) as "totaldatabytes", round(min("ipat"), 2) as "minipat",
          round(max("ipat"),2) as "maxipat", round(avg("ipat"),2) as "avgipat",
          when(stddev("ipat") === "NaN", 0.0).otherwise(round(stddev("ipat"),2)) as "stddevipat",
          round((max("collectiontime") - min("collectiontime"))/1000000, 3) as "flowduration"
        )
      System.out.println("Tagging Packets: "+groupResult.count())

      //Add processing time to data frame
      val result = groupResult.withColumn("processing_time", processing_time).cache()

      //Save result to parquet file format
      result.toDF.write.parquet("/home/netcs/Multi-View-Staged/AggregateControlPlane.parquet")

      //result.filter("measurementboxname like 'SmartX-Box-GIST1'").show(1000, false)

      // Stage data to ElasticSearch DataStore
      result.write.mode("append")
        .format("org.elasticsearch.spark.sql")
        .option("es.mapping.date.rich", "false")
        .save("cp-staged-flows/summarized")

      /*val notTagged = AppTaggedFlow.filter("appclassid is null")

      // Stage data to ElasticSearch DataStore
      EsSparkSQL.saveToEs(notTagged, "cp-clustered-flows/inspected")*/

      //Remove raw data file from disk
      for (file <- files) {
        Predef.println("Deleted Raw File: " + file.toString)

        FileUtils.deleteQuietly(new File(file.toString))
      }
    }
}

  //Define classes to validate collected visibility data
  case class ValidateDataPlane(collectiontime: Double, measurementboxname: String, measurementboxip: String, ipversion: Int, src_host: String, dest_host: String, src_host_port: Int, dest_host_port: Int, src_vm_ip: String, dest_vm_ip: String, src_vm_port: Int, dest_vm_port: Int, vni: Int, vlan: Int, protocol: Int, tcp_window_size: Int, databytes: Int)

  case class ValidateControlPlane(collectiontime: Double, net_plane: String, measurementboxname: String, measurementboxip: String, ipversion: Int, src_host: String, dest_host: String, src_host_port: Int, dest_host_port: Int, protocol: Int, tcp_window_size: Int, databytes: Int)

  case class TenantsTagClass(vlanid: String, tenantid: String, tenantname: String, subnet: String)

  case class ControlTagsClass(tenantid: String, tenantname: String, hostid: String, vmid: String, controlip: String)

  case class VMTagsClass(tenantids: String, tenantnames: String, vmid: String, dataip: String)

  case class AppsTagsClass(protocolid: Int, tagids: Int, appport: Int, tenantids: String)

  case class TagsMappingClass(tagid: Int, appclassids: Int, appclassname: String, appids: Int, appname: String)

}
