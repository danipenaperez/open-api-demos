package com.example.democlient;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.dppware.demo.client.api.DefaultApi;
import com.dppware.demo.client.invoker.ApiClient;

@SpringBootApplication
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

		// SpringApplication.run(DemoClientApplication.class, args);
	}

}
