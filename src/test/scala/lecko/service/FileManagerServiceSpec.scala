package lecko.service

import java.io.File
import java.nio.file.{Files, Paths}

import lecko.service.model.ModelJsonProtocol._
import lecko.service.model.{DeleteResult, ExistsResult, UploadFileResult, UploadFileResults}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http._
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

@RunWith(classOf[JUnitRunner])
class FileManagerServiceSpec extends FlatSpec with ScalatestRouteTest
with HttpService with FileManagerService with Matchers with BeforeAndAfterAll {
  override implicit def actorRefFactory = system

  override def pathFiles = {
    val path = "/tmp/files/"
    new File(path).mkdirs()
    path
  }

  "The service" should  "return an \"up and running\" for GET requests to the root path" in {
      Get() ~> route ~> check {
        responseAs[String] should be (index.toString())
      }
    }

  "The service" should  "return a No exist response" in {
    Get("/file/2011/09/13/test.txt?exists=true") ~> route ~> check {
      responseAs[String] should be ("{\n  \"exists\": false\n}")
    }
  }

  "The service" should "persist the file in filesystem" in {
    def file: File = new File(pathFiles + "2014/09/13/file.txt")
    file.delete()
    val bodyPart = BodyPart(HttpEntity("Hola Mundo".getBytes),Seq(`Content-Disposition`("form-data",Map("name" -> "part1",
      "filename"->"file.txt"))))
    val formData = MultipartFormData(Seq(bodyPart))
    Post("/file/2014/09/13", formData)  ~> route ~> check {
      responseAs[UploadFileResults] should be (UploadFileResults(List(UploadFileResult("file.txt", true, None))))
    }
    file.exists() should be (true)
    scala.io.Source.fromFile(file).toList.mkString should be ("Hola Mundo")
  }


  "The service" should "delete existent file" in {
    Delete("/file/2011/09/13/toDelete.txt")  ~> route ~> check {
      responseAs[DeleteResult] should be (DeleteResult(true))
    }
  }

  "The service" should "get existent file" in {
    Get("/file/2011/09/13/toGet.txt")  ~> route ~> check {
      responseAs[String] should be ("Hola Mundo")
    }
  }

  "The service" should "validate unexistent file" in {
    Get("/file/2011/09/13/noFile.txt?exists=true")  ~> route ~> check {
      responseAs[ExistsResult] should be (ExistsResult(false))
    }
  }

  "The service" should "validate existent file" in {
    Get("/file/2011/09/13/toGet.txt?exists=true")  ~> route ~> check {
      responseAs[ExistsResult] should be (ExistsResult(true))
    }
  }

  override protected def beforeAll() = {
    super.beforeAll()
    //new File(pathFiles + "2011/09/13/").mkdirs()
    Files.createDirectories(Paths.get(pathFiles + "2011/09/13/"))
    Files.write(Paths.get(pathFiles + "2011/09/13/toDelete.txt"), "Hola Mundo".getBytes)
    Files.write(Paths.get(pathFiles + "2011/09/13/toGet.txt"), "Hola Mundo".getBytes)
  }
}
