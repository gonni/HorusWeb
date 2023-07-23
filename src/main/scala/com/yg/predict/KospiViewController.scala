package com.yg.predict

import com.yg.data.KospiRepo
import org.scalatra.{FutureSupport, ScalatraServlet}
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._
import com.yg.ai.stock.kospidata._
import com.yg.ai.stock.predict.PredictService

trait KospiViewController extends ScalatraServlet with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)
  def db: Database
  val predictService = PredictService.getInstance

  get("/kospiIndex") {
    logger.info("detected kospi predict")

    val testData = Await.result(db.run(KospiRepo.findLatest(70).sortBy(_.targetDt.asc).result), Duration.Inf)

    val javaTestData = testData.map(row => new PartKospiData(row.targetDt, row.indexValue.get, row.totalEa.get, row.totalVolume.get))
    javaTestData.foreach(println)

    // make xValue string
    val kiv = predictService.predict(javaTestData.asJava, 3).asScala.head
    val predicted = kiv.getPredicted
    val xValues = testData.takeRight(predicted.size()).map(r => {
      val df = r.targetDt.split("\\.")
      if(df(2).toInt % 5 == 0) {
        "'" + df(1) + "/" + df(2) + "'"
      } else {
        ""
      }
    })

    val latestDay = testData(testData.size - 1).targetDt.split("\\.")
    val xValues2 = xValues.take(xValues.size -4) ++ Seq("'" + latestDay(1) + "/" + latestDay(2)+"'","","","'D+3'")
    val xValuesString = xValues2.mkString(",")

    println("->" + xValuesString)
    // ~xValue string


    val mainPage = com.yg.predict.html.kospi(
      kiv.getPredicted.asScala.take(kiv.getPredicted.size()-3).mkString(","),
      (0 to kiv.getPredicted.size() - 4).map(r => "").mkString(",") + kiv.getPredicted.asScala.takeRight(4).mkString(","),
      kiv.getActual.asScala.take(kiv.getActual.size()-3).mkString(","),
      xValuesString)
      //(0 to kiv.getPredicted.asScala.length).mkString(","))
    layouts.html.dashboard.render("Predict KOSPI", mainPage);
  }
}

class KospiViewControllerImpl(val db: Database) extends ScalatraServlet with FutureSupport with KospiViewController {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}