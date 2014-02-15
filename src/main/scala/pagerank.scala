import spark.SparkContext
import spark.SparkContext._

class Page(val title:String, val links:Array[String]) {}

object PageRank{
        def parsePage(line:String): Page = {
                val pieces = line.split("[:,]")
                val title = pieces(0)
                val links = pieces.tail.map(_.trim)
                new Page(title, links)
        }

        def main(args:Array[String]) {

                val masterUrl = args(0)
                val inputFile = args(1)
                val numIteration = args(2).toInt

                val SparkHome = "/home/software/spark"
		val jarFile = "target/scala-2.9.2/pagerank_2.9.2-1.0.jar"
		
		val sc = new SparkContext(masterUrl, "pagerank", SparkHome, Seq(jarFile))

		val pages = sc.textFile(inputFile).map(parsePage)
		val links = pages.map(p=>(p.title,p.links))
		var ranks = pages.map(p=>(p.title,1.0))
		links.cache()

		for(iteration <- 0 until numIteration) {
			val contribs = links.join(ranks).flatMap{
				case (title, (neighbors, rank)) =>
					neighbors.map(n=>(n, rank/neighbors.size))
			}
			ranks = contribs.reduceByKey(_+_).mapValues(0.15 + 0.85 * _)
		}
		println("Final Ranks:")
		ranks.collect().foreach(println)
		sc.stop()

        }
}

