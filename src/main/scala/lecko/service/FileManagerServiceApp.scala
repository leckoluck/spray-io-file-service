package lecko.service

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import org.apache.commons.cli.{Option => OptCli, HelpFormatter, BasicParser, Options}
import spray.can.Http

object FileManagerServiceApp extends App {

  implicit val system = ActorSystem()

  val (path:String, port:Int) = {
    findArgs() match {
      case Some((x,y)) => (x,y)
      case None => System.exit(-1)
    }
  }
  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[FileServiceActor], name = "handler")


  IO(Http) ! Http.Bind(handler,  interface = "0.0.0.0", port = port)



  def findArgs(): Option[(String, Int)] = {
    val filePathName= "file-path"
    val portName = "port"
    val sh: String = "file-manager-service.sh"


    val defaultPort = 26814
    val filePath = {
      val p = new OptCli( "f", filePathName, true, "root file path" )
      p.setRequired(true)
      p
    }

    val port = {
      val p = new OptCli( "p",portName, true, "port number" )
      p.setType(classOf[Int])
      p.setRequired(false)
      p
    }
    val options = new Options()
    options.addOption( filePath )
    options.addOption( port)


    val parser = new BasicParser()
    try {
      // parse the command line arguments
      val line = parser.parse( options, args )
      if( line.hasOption( filePathName )) {
        val p = if (line.hasOption(portName)) {
          line.getOptionValue(portName, defaultPort.toString).toInt
        } else {
          defaultPort
        }
        val path = line.getOptionValue(filePathName)
        Option((path, p))
      } else {
        val formatter = new HelpFormatter()
        formatter.printHelp( sh, options)
        None
      }

    }
    catch {
      case exp:Throwable =>
        val formatter = new HelpFormatter()
        formatter.printHelp( sh, options)
        // oops, something went wrong
        None
//        System.err.println( "Parsing failed.  Reason: " + exp.getMessage)
//        throw exp
    }
  }
}

