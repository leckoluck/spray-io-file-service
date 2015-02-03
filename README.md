# spray-io-file-service

This is an example of using spray.io 1.3.2, akka 2.3.7 and scala 2.11.

This is a simple service where you can store/get/delete files.

You can run the tests with


````
gradle test
````

To test the application you can

````
gradle clean build distZip

cd build/distributions/

unzip file-manager-service.zip

cd file-manager-service/bin

./file-manager-service.sh --file-path /tmp/files
````

You can then upload multiple files using curl, e.g.,

````
curl -F f1=@example.json -F f2=@json160.gif http://localhost:26814/file/2015/02/11
````

and then get it:

````
curl http://localhost:26814/file/2015/02/11/example.json
````

or check if exists

````
curl http://localhost:26814/file/2015/02/11/example.json?exists=true
````

or delete

````
curl -X "DELETE" http://localhost:26814/file/2015/02/11/example.json
````

https://github.com/whataboutbob/spray-file-upload.git was taken as reference

