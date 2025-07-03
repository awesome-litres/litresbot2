package litresbot.download;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.net.URI;
import java.nio.file.Paths;
import java.util.HashSet;

public class S3BookClient {
    protected S3Client client;
    protected String bucketName;
    protected String bookPath;
    protected String downloadPath;

    public S3BookClient(String s3Url, String accessKey, String secretKey, String regionName, String bucketName, String bookPath) {
        final var credentials = AwsBasicCredentials.create(accessKey, secretKey);
        var clientBuilder = S3Client
            .builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .forcePathStyle(true);
        if (regionName != null && !regionName.isEmpty()) {
            clientBuilder = clientBuilder.region(Region.of(regionName));
        }
        if (s3Url != null && !s3Url.isEmpty()) {
            clientBuilder = clientBuilder.endpointOverride(URI.create(s3Url));
        }
        client = clientBuilder.build();
        this.bucketName = bucketName;
        bookPath = Paths.get(bookPath).toString().replace("\\", "/");
        if (!bookPath.endsWith("/")) {
            bookPath += "/";
        }
        this.bookPath = bookPath;
    }

    public String getBookPath() {
        return bookPath;
    }

    public int downloadFile(String fileName, String saveFileName) {
        // Ensure the folder at savePath exists
        final var savePath = Paths.get(saveFileName);
        final var saveDir = savePath.getParent();
        if (saveDir != null && !saveDir.toFile().exists()) {
            saveDir.toFile().mkdirs();
        }

        // Ensure the file at savePath does not exist
        final var saveFile = savePath.toFile();
        if (saveFile.exists()) {
            saveFile.delete();
        }

        final var loadPath = Paths.get(bookPath, fileName).toString().replace("\\", "/");
        final var request = GetObjectRequest
            .builder()
            .bucket(bucketName)
            .key(loadPath)
            .build();

        final var response = client.getObject(request, ResponseTransformer.toFile(savePath));
        return response.sdkHttpResponse().statusCode();
    }

    public boolean fileExists(String fileName) {
        final var filePath = Paths.get(bookPath, fileName).toString().replace("\\", "/");
        try {
            final var request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();
            
            client.headObject(request);
            return true;
        } catch (NoSuchKeyException _e) {
        }
        return false;
    }

    public HashSet<String> listFiles(String directory) {
        var files = new HashSet<String>();
        final String dirPath;
        {
            var dirPathTemp = Paths.get(bookPath, directory).toString().replace("\\", "/");
            if (!dirPathTemp.endsWith("/")) {
                dirPathTemp += "/";
            }
            dirPath = dirPathTemp;
        }
        String continuationToken = null;
        while (true) {
            var listRequestBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(dirPath)
                .delimiter("/");
            if (continuationToken != null) {
                listRequestBuilder = listRequestBuilder.continuationToken(continuationToken);
            }
            final var listRequest = listRequestBuilder.build();
            final var listResponse = client.listObjectsV2(listRequest);
            if (listResponse.sdkHttpResponse().statusCode() != 200) {
                throw new RuntimeException("Failed to list files: " + listResponse.sdkHttpResponse().statusText().orElse("Unknown error"));
            }

            final var contents = listResponse.contents();
            final var filesChunk = contents.stream().map(S3Object::key);
            files.addAll(filesChunk.toList());
            final var dirsChunk = listResponse.commonPrefixes().stream().map(CommonPrefix::prefix);
            files.addAll(dirsChunk.toList());

            if (!listResponse.isTruncated()) {
                break;
            }
            continuationToken = listResponse.continuationToken();
        }
        // remove prefix of each file
        final var truncFiles = files
            .stream()
            .map(file -> file.startsWith(dirPath) ? file.substring(dirPath.length()) : file)
            .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        return truncFiles;
    }
}
