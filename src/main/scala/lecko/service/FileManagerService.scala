package lecko.service

import java.io.{File, InputStream, OutputStream}
import java.nio.file.{Paths, Path, Files}

import spray.http.MediaTypes._
import spray.http.{BodyPart, _}
import spray.routing._
import lecko.service.model.{UploadFileResults, UploadFileResult, DeleteResult, ExistsResult}
import lecko.service.model.ModelJsonProtocol._
import spray.json._

// this trait defines our service behavior independently from the service actor
trait FileManagerService extends HttpService {

  // we use the enclosing ActorContext's or ActorSystem's dispatcher for our Futures and Scheduler
  implicit def executionContext = actorRefFactory.dispatcher

  private val fileSep = File.separator
  def pathFiles = {
    val f = FileManagerServiceApp.path
    if (f.endsWith("/")) f else f + "/"
  }

  val route: Route = {
    get {
      path("") {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete(index)
        }
      }
    } ~
      path("file"/ Segment / Segment / Segment / Segment) { (year, month, day, fileName) =>
        get {
          import java.nio.file.{Files, Paths}
          val path = Paths.get(pathDir(year, month, day) + fileSep +  fileName)
          val exists = Files.exists(path)
          parameters('exists ?) {
            case Some("true") =>
              respondWithMediaType(`application/json`) {
                complete {
                  ExistsResult(exists).toJson.prettyPrint
                }
              }
            case _ =>
              if (exists) {
                complete {
                  val buf = Files.readAllBytes(path)
                  spray.http.HttpData(buf)
                }
              } else  {
                complete(StatusCodes.BadRequest, "No Content found")
              }
          }
        } ~
          delete {
            respondWithMediaType(`application/json`) {
              complete {
                val file = new File (pathDir(year, month, day) + fileSep + fileName)
                DeleteResult(file.delete).toJson.prettyPrint
              }
            }
          }

    } ~
    path("file" / Segment / Segment / Segment) { (year, month, day) =>
      post {
        respondWithMediaType(`application/json`) {
          entity(as[MultipartFormData]) { formData =>
            detach() {
              complete {
                val details = formData.fields.map {
                  case (BodyPart(entity, headers)) =>
                    val content = entity.data.toByteArray
                    val fileName = headers.find(h => h.is("content-disposition")).get.value.split("filename=").last
                    val (result, error) = saveAttachment(year, month, day, fileName, content)
                    Option(UploadFileResult(fileName, result, error))
                  case _ => None
                }
                UploadFileResults(details.toList.filter(_.isDefined).map(_.get)).toJson.prettyPrint
              }
            }
          }
        }
      }
    }
  }

  lazy val index =
    <html>
      <body>
        <h1>File Manager Up and Running.</h1>
      </body>
    </html>


  private def saveAttachment(year:String, month:String, day:String, fileName: String, content: Array[Byte]): (Boolean, Option[String])= {
    saveAttachment[Array[Byte]](year:String, month:String, day:String, fileName, content, {(is, os) => os.write(is)})
  }

  private def saveAttachment[T](year:String, month:String, day:String, fileName: String, content: T, writeFile: (T, OutputStream) => Unit): (Boolean, Option[String]) = {
    try {
      val path = pathDir(year, month, day)
      Files.createDirectories(Paths.get(path))
      val fos = new java.io.FileOutputStream(path + fileSep + fileName)
      writeFile(content, fos)
      fos.close()
      (true, None)
    } catch {
      case e:Throwable => (false, Some(e.getMessage))
    }
  }

  def pathDir(year:String, month:String, day:String) = pathFiles + year + fileSep + month + fileSep + day

}
