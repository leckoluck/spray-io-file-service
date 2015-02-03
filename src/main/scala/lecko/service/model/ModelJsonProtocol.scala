package lecko.service.model

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object ModelJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val existsResultFormat = jsonFormat1(ExistsResult)
  implicit val deleteResultFormat = jsonFormat1(DeleteResult)
  implicit val uploadFileResultFormat = jsonFormat3(UploadFileResult)
  implicit val uploadFileResultsFormat = jsonFormat1(UploadFileResults)

}
