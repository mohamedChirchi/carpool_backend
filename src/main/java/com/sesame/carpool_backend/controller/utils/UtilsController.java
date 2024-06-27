package com.sesame.carpool_backend.controller.utils;

import com.sesame.carpool_backend.service.utils.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/uploads")
public class UtilsController {
    @Autowired
    private FileStorageService fileStorage;

    @GetMapping("/img/user/{filename}")
    @ResponseBody
    public ResponseEntity<?> getImgUser(@PathVariable String filename) {
        Resource file = fileStorage.loadImgUser(filename);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"");
        String contentType = MediaTypeFactory.getMediaType(file).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        headers.setContentType(MediaType.parseMediaType(contentType));

        return ResponseEntity.ok().headers(headers).body(file);
    }

    @GetMapping("/img/default/{filename}")
    @ResponseBody
    public ResponseEntity<?> getIDefaultImg(@PathVariable String filename) {

        Resource file = fileStorage.loadDefaultImg(filename);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"");
        String contentType = MediaTypeFactory.getMediaType(file).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        headers.setContentType(MediaType.parseMediaType(contentType));

        return ResponseEntity.ok().headers(headers).body(file);
    }
}
