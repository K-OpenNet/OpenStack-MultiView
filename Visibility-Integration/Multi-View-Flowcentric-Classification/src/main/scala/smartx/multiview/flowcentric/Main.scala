package smartx.multiview.flowcentric

import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.functions.{current_timestamp, from_unixtime, unix_timestamp}

object Main {
  def main(args: Array[String]): Unit = {
    //Verify Visibility Center IP is provided.
    if (args.length < 2) {
      System.out.println("Please provide Visibility Center IP and Root path to Integrated Data Files as an arguments. e.g. x.x.x.x /home/netcs")
      System.exit(1)
    }

    val VISIBILITY_CENTER_IP = args(0)
    val ROOT_PATH_TO_DATA = args(1)

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

    classify_multilayer_visibility()
    spark.close()

    //Underlay Network Resource-layer Data Summarization
    def classify_multilayer_visibility(): Unit = {
      //Load data from Staging DataStore
      val IntegratedDF = spark.read.load(ROOT_PATH_TO_DATA + "/Multi-View-Integrated/data.parquet")
      IntegratedDF.count()

      //Load data from attacked IPs
      val AttackersFile = spark.read.format("csv").option("header", "false").load(ROOT_PATH_TO_DATA + "/Multi-View-Attackers/attackers")
      var AttackersDF = AttackersFile.map(p => BlackListedIPClass.apply(p(0).toString)).toDF()
      AttackersDF = AttackersDF.distinct()
      //AttackersDF.count()

      //Load data from Staging DataStore
      //val DPlanesDF = IntegratedDF.withColumnRenamed("processed_at", "processingTime")

      //Check for network packets from Blacklisted IP addresses
      //val TagAttacked = IntegratedDF.join(right = AttackersDF, col("src_vm_ip") === col("ipaddress") || col("dest_vm_ip") === col("ipaddress"), joinType = "left_outer").withColumn("classified",lit("Attack")).withColumn("actiontaken", lit("N")).withColumnRenamed("processed_at", "processingTime")
      val TagAttacked = IntegratedDF.join(AttackersDF, col("src_vm_ip") === col("ipaddress") || col("dest_vm_ip") === col("ipaddress")).withColumnRenamed("processed_at", "processingTime")
      System.out.println("Attack Classified: "+TagAttacked.count())
      TagAttacked.show(false)

      //Separate attacked network packets
      //val AttackedResult = TagAttacked.filter($"classified".isNotNull)
      val AttackedResult = TagAttacked.drop("ipaddress")

      //Separate not attacked network packets
      //val NotAttackedResult = TagAttacked.filter($"classified".isNull).drop("classified")

      //Separate identified packets
      var IdentifiedResult = (IntegratedDF.filter($"tenantname".isNotNull && $"appname".isNotNull)).except(AttackedResult)//.withColumn("classified",lit("Identify")).withColumn("actiontaken", lit("N"))

      //Separate known packets
      var KnownResult = (IntegratedDF.filter($"tenantname".isNotNull)).except(IdentifiedResult)

      //Separate unknown packets
      var UnknownResult = IntegratedDF.filter($"tenantname".isNull && $"appname".isNull).except(KnownResult)

      //Remove previous data files from the disk
      val dir = new File(ROOT_PATH_TO_DATA + "/Multi-View-Classified/")
      FileUtils.deleteDirectory(dir)

      //Save result to Parquet Data Store
      AttackedResult.toDF.write.parquet(ROOT_PATH_TO_DATA + "/Multi-View-Classified/dp-classified-attack-1")
      IdentifiedResult.toDF.write.parquet(ROOT_PATH_TO_DATA + "/Multi-View-Classified/dp-classified-identify-1")
      KnownResult.toDF.write.parquet(ROOT_PATH_TO_DATA + "/Multi-View-Classified/dp-classified-known-1")
      UnknownResult.toDF.write.parquet(ROOT_PATH_TO_DATA + "/Multi-View-Classified/dp-classified-unknown-1")

      //Save data to ElasticSearch Data Store
      AttackedResult.write.mode("append")
        .format("org.elasticsearch.spark.sql")
        .option("es.mapping.date.rich", "false")
        .save("dp-classified-attack-1/attack-classified")

      IdentifiedResult.write.mode("append")
        .format("org.elasticsearch.spark.sql")
        .option("es.mapping.date.rich", "false")
        .save("dp-classified-identify-1/identify-classified")

      KnownResult.write.mode("append")
        .format("org.elasticsearch.spark.sql")
        .option("es.mapping.date.rich", "false")
        .save("dp-classified-known-1/known-classified")

      UnknownResult.write.mode("append")
        .format("org.elasticsearch.spark.sql")
        .option("es.mapping.date.rich", "false")
        .save("dp-classified-unknown-1/unknown-classified")
    }
  }

  //Define Blacklisted IPs data validation class
  case class BlackListedIPClass(ipaddress: String)
}

