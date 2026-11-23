package com.example.pro3.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Service
public class HdfsService {

    @Value("${app.hdfs.uri:hdfs://localhost:9000}")
    private String hdfsUri;

    public byte[] getImage(String filename) throws IOException {
        Configuration conf = new Configuration();
        Path path = new Path("/user/images/" + filename);

        try (FileSystem fs = FileSystem.newInstance(URI.create(hdfsUri), conf)) {
            if (!fs.exists(path)) {
                return null;
            }
            try (FSDataInputStream in = fs.open(path)) {
                return in.readAllBytes();
            }
        }
    }
}