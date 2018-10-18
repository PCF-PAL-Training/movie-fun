package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.nio.file.Files.readAllBytes;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        saveUploadToFile(blob, getCoverFile(blob.name));
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        // TODO Improve error handling

        Path coverFilePath = null;
        try {
            coverFilePath = getExistingCoverPath(name);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        byte[] imageBytes = readAllBytes(coverFilePath);

        return Optional.of(new Blob(
                        coverFilePath.toString(),
                        new ByteArrayInputStream(imageBytes),
                        new Tika().detect(coverFilePath)));
    }

    @Override
    public void delete(String path) {

    }

    @Override
    public void deleteAll() {
        // ...
    }

    private void saveUploadToFile(Blob blob, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        byte[] targetArray = new byte[blob.inputStream.available()];
        blob.inputStream.read(targetArray);

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(targetArray);
        }
    }

    private File getCoverFile(String albumId) {
        //return new File(format("covers/%d", albumId));
        return new File("covers/" + albumId);
    }

    private Path getExistingCoverPath(String albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }
}