package lecko.service.model

case class ExistsResult(exists:Boolean)
case class DeleteResult(result:Boolean)
case class UploadFileResults(results:List[UploadFileResult])
case class UploadFileResult(fileName:String, result:Boolean, error:Option[String])




