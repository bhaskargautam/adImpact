import java.sql.Timestamp
import scala.io.Source
import scala.util.parsing.json._
import java.text.SimpleDateFormat
import java.util.Date

object AdImpact {

  //Constants defination
  val filePath = "/Users/bhaskargautam/adimpact/new_users.json"
  val tenMinutes = 600000 // 10 min in miliseconds

  def get_date(a: String, f: SimpleDateFormat): Date = {
    return f.parse(a.replace("T", " "))
  }

  def main(args: Array[String]): Unit = {
    //Read input file
    val lines = Source.fromFile("/Users/bhaskargautam/adimpact/new_users.json").getLines.toList
    val json:Option[Any] = JSON.parseFull(lines.mkString("\n"))
    val map:Map[String, List[Map[String, String]]] = json.get.asInstanceOf[Map[String, List[Map[String, String]]]]

    val format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss")
    var count:Map[String, Int] = Map()

    map.get("tvSpots").foreach(m => m.foreach(n =>
        map.get("newUsers").foreach(x => x.foreach( y => {
            val spotTime = get_date(n.get("time").last, format).getTime
            val userTime = get_date(y.get("time").last, format).getTime

            if(userTime > spotTime) {
                if(userTime - spotTime < tenMinutes)
                    //Add to current spot
                    if(count.contains(n.get("spotId").last))
                       count += (n.get("spotId").last -> (count.get(n.get("spotId").last).last + 1))
                    else
                       count += (n.get("spotId").last -> 1)
            }
            else {
                if(spotTime - userTime < tenMinutes)
                    //Subtract from current spot
                    if(count.contains(n.get("spotId").last))
                       count += (n.get("spotId").last -> (count.get(n.get("spotId").last).last - 1))
                    else
                       count += (n.get("spotId").last -> -1)
            }
        }))))

    //Print count for each spot
    map.get("tvSpots").foreach(m => m.foreach(n =>
            println("Spot %s: %d new users".format(n.get("spotId").last, count.get(n.get("spotId").last).last))
        ))

  }

}