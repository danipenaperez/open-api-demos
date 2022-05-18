package com.example.demo.controller;

import java.util.Locale;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.dppware.demo.server.api.DownloadPdfApi;

import lombok.AllArgsConstructor;


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