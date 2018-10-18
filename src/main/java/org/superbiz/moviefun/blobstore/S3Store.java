package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.tika.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {

    private final AmazonS3Client s3Client;
    private final String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {

        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(blob.contentType);
        metadata.setContentLength(blob.inputStream.available());

        PutObjectRequest request = new PutObjectRequest(photoStorageBucket, blob.name, blob.inputStream, metadata);
        request.setMetadata(metadata);

        s3Client.putObject(request);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {

        GetObjectRequest getObjectRequestHeaderOverride = new GetObjectRequest(photoStorageBucket, name);

        S3Object s3Object = s3Client.getObject(getObjectRequestHeaderOverride);

        byte [] bytes = IOUtils.toByteArray(s3Object.getObjectContent());

        Blob blob = new Blob(name, new ByteArrayInputStream(bytes), s3Object.getObjectMetadata().getContentType());
        return Optional.of(blob);
    }

    @Override
    public void delete(String path) {
        DeleteObjectRequest request = new DeleteObjectRequest(photoStorageBucket, path);
        s3Client.deleteObject(request);
    }

    @Override
    public void deleteAll() {

    }
}