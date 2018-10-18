package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }

    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {

        Blob blob = new Blob(Long.toString(albumId), uploadedFile.getInputStream(), uploadedFile.getContentType());
        blobStore.put(blob);
        return format("redirect:/albums/%d", albumId);
    }

    @PostMapping("/{albumId}/cover/delete")
    public String deleteCover(@PathVariable long albumId) throws IOException {
        blobStore.delete(Long.toString(albumId));
        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Optional<Blob> blob = blobStore.get(Long.toString(albumId));

        byte[] targetArray = new byte[blob.get().inputStream.available()];
        blob.get().inputStream.read(targetArray);

        HttpHeaders headers = createImageHttpHeaders(blob.get().name, targetArray.length);
        return new HttpEntity<>(targetArray, headers);
    }

    private HttpHeaders createImageHttpHeaders(String coverFilePath, int dataLength) throws IOException {

        String contentType = new Tika().detect(coverFilePath);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(dataLength);
        return headers;
    }
}