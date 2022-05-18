
# Consideraciones

Las versiones de swagger y swagger code gen pierden compatibilidad con java 11 (ya que no existe @javax.annotation.Generated)
Esta demo usa la imagen docker oficial de openapigenerator para la construccion de artefactos.

# Proposito

Este repositorio muestra como generar un artefacto java que contiene los stubs de un fichero de openapi (swagger).
En este caso solo existe un endpoint que devuelve un binario (application/pdf), entonces lo que se trata es de 2 cosas

- Probar como generar las interfaces en las que se basara nuestro servidor para implemetar el contrato 
- Probar como se documenta un endpoint que devuelve un binario (en este caso pdf que tenemos en la carpeta del proyecto /demo-server/pdffiles).

# Contrato de open api 3 ()
Se encuentra en el fichero /api-contracts/openapi3.yaml , en esta primera version solo tiene 1 endpoint

# GENERANDO SERVER

## Generacion de codigo 
Para generar el codigo a partir de ese contrato tenemos el ejecutabel generate-server.sh :

```sh  
ARTIFACT_ID=pdf-api
ARTIFACT_VERSION=1.0.0
ARTIFACT_PACKAGE=com.dppware.demo
OPENAPI_MAIN_FILE=./swagger/swagger.yaml

docker run --net host --rm \
-v "${PWD}/api-contracts:/local" openapitools/openapi-generator-cli generate \
-i /local/$OPENAPI_MAIN_FILE \
--api-package $ARTIFACT_PACKAGE.server.api \
--model-package $ARTIFACT_PACKAGE.server.model \
--group-id $GROUPID \
--artifact-id $ARTIFACT_ID \
--artifact-version $ARTIFACT_VERSION \
-g spring \
-p java8=true \
--additional-properties interfaceOnly=true \
--additional-properties generateSupportingFiles=false \
--enable-post-process-file \
-o /local/generated/$ARTIFACT_ID
```  
Esto acaba generando un proyecto Maven en /api-contracts/generated/spring-pdf-api-server. El propio script lo compila y lo instala
```  
echo "Compile and Install artifact"
cd ./api-contracts/generated/$ARTIFACT_ID
mvn clean install
...
...
[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ spring-pdf-api-server
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
``` 

## Implementando el contrato
Para ello tenemos el proyecto /demo-server, que es un proyecto maven vacio. Importamos las interfaces del api incluyendo en el pom.xml
```xml 
...
		<dependency>
                <groupId>com.dppware</groupId> 
                <artifactId>spring-pdf-api-server</artifactId> 
                <version>1.0.0</version> 
		</dependency>
...

``` 
Creamos una clase que implemente la clase del API (DownloadPdfApi.java) de la libreria importada, nos quedaria asi:
```java

@org.springframework.stereotype.Controller
@AllArgsConstructor
public class PDFController implements DownloadPdfApi {

    public ResponseEntity<byte[]> downloadPDF() {
    	
    	Locale loc = new Locale("es");
        Path file = null;
        byte[] contents = null;
        try {
			file = Paths.get("./pdffiles/sample.pdf");
			contents = Files.readAllBytes(file);
		} catch (Exception e) {
			e.printStackTrace();
		}         
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(file.getFileName().getFileName().toString(), file.getFileName().toString());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<>(contents, headers, HttpStatus.OK);
        return response;
    }

}


``` 

## Probando la solucion
Arrancamos el servidor:
```sh 
$ cd demo
$ mvn clean install spring-boot:run 
....
 Root WebApplicationContext: initialization completed in 492 ms
o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
com.example.demo.DemoApplication         : Started DemoApplication in 0.944 seconds (JVM running for 1.159)
``` 

Y visitamos la url  http://localhost:8080/download-pdf y vemos que se descarga el fichero pdf .

# GENERANDO EL CLIENTE
A partir del mismo contrato podemos generar el codigo para obtener un cliente de api.


## Generacion de codigo 
Para generar el codigo a partir de ese contrato tenemos el ejecutabel generate-client.sh :

```sh  
ARTIFACT_ID=pdf-api
ARTIFACT_VERSION=1.0.0
ARTIFACT_PACKAGE=com.dppware.demo
OPENAPI_MAIN_FILE=./swagger/swagger.yaml

docker run --net host --rm \
-v "${PWD}/api-contracts:/local" openapitools/openapi-generator-cli generate \
-i /local/$OPENAPI_MAIN_FILE \
-g java \
--library resttemplate \
--additional-properties interfaceOnly=true \
--additional-properties generateSupportingFiles=false \
--additional-properties dateLibrary=java8 \
--enable-post-process-file \
--api-package $ARTIFACT_PACKAGE.client.api \
--model-package $ARTIFACT_PACKAGE.client.model \
--model-name-suffix DTO \
--invoker-package $ARTIFACT_PACKAGE.client.invoker \
--group-id $GROUPID \
--artifact-id $ARTIFACT_ID \
--artifact-version $ARTIFACT_VERSION \
-o /local/generated/$ARTIFACT_ID

```  
Esto acaba generando un proyecto Maven en /api-contracts/generated/spring-pdf-api-client. El propio script lo compila y lo instala
```  
echo "Compile and Install artifact"
cd ./api-contracts/generated/$ARTIFACT_ID
mvn clean install
...
...
[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ spring-pdf-api-client
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
``` 

## Implementando el contrato
Para ello tenemos el proyecto /demo-client, que es un proyecto maven vacio. Importamos las interfaces del api incluyendo en el pom.xml
```xml 
...
		<dependency>
                <groupId>com.dppware</groupId> 
                <artifactId>spring-pdf-api-client</artifactId> 
                <version>1.0.0</version> 
		</dependency>
...

``` 
Creamos una clase main que use el cliente de la libreria importada, nos quedaria asi:
```java

public class DemoClientApplication {

	public static void main(String[] args) {
		try {
			DefaultApi a = new DefaultApi();
			ApiClient apiClient = new ApiClient();
			apiClient.setBasePath("http://localhost:8080");

			a.setApiClient(apiClient);
			byte[] result = a.downloadPDF();
			System.out.println("Fetched byte info : " + result);
			OutputStream out = new FileOutputStream("target/sample.pdf");
			out.write(result);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}


``` 

## Probando la solucion
Arrancamos el servidor:
```sh 
$ cd demo-server
$ mvn clean install spring-boot:run 
....
 Root WebApplicationContext: initialization completed in 492 ms
o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
com.example.demo.DemoApplication         : Started DemoApplication in 0.944 seconds (JVM running for 1.159)
``` 

Y para probar el cliente ejecutamos la clase DemoClientApplication.java y vemos que se descarga un pdf en /target/sample.pdf
