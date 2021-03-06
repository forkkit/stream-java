package io.getstream.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.getstream.core.utils.Request.*;
import static io.getstream.core.utils.Routes.buildImagesURL;
import static io.getstream.core.utils.Serialization.deserialize;
import static io.getstream.core.utils.Serialization.deserializeError;

import io.getstream.core.exceptions.StreamException;
import io.getstream.core.http.HTTPClient;
import io.getstream.core.http.Token;
import io.getstream.core.options.CustomQueryParameter;
import io.getstream.core.options.RequestOption;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java8.util.concurrent.CompletableFuture;
import java8.util.concurrent.CompletionException;

public class StreamImages {
  private final String key;
  private final URL baseURL;
  private final HTTPClient httpClient;

  StreamImages(String key, URL baseURL, HTTPClient httpClient) {
    this.key = key;
    this.baseURL = baseURL;
    this.httpClient = httpClient;
  }

  public CompletableFuture<URL> upload(Token token, String fileName, byte[] content)
      throws StreamException {
    checkNotNull(content, "No data to upload");
    checkArgument(content.length > 0, "No data to upload");

    try {
      final URL url = buildImagesURL(baseURL);
      return httpClient
          .execute(buildMultiPartPost(url, key, token, fileName, content))
          .thenApply(
              response -> {
                try {
                  return deserialize(response, "file", URL.class);
                } catch (StreamException | IOException e) {
                  throw new CompletionException(e);
                }
              });
    } catch (MalformedURLException | URISyntaxException e) {
      throw new StreamException(e);
    }
  }

  public CompletableFuture<URL> upload(Token token, File content) throws StreamException {
    checkNotNull(content, "No file to upload");
    checkArgument(content.exists(), "No file to upload");

    try {
      final URL url = buildImagesURL(baseURL);
      return httpClient
          .execute(buildMultiPartPost(url, key, token, content))
          .thenApply(
              response -> {
                try {
                  return deserialize(response, "file", URL.class);
                } catch (StreamException | IOException e) {
                  throw new CompletionException(e);
                }
              });
    } catch (MalformedURLException | URISyntaxException e) {
      throw new StreamException(e);
    }
  }

  public CompletableFuture<Void> delete(Token token, URL targetURL) throws StreamException {
    checkNotNull(targetURL, "No image to delete");

    try {
      final URL url = buildImagesURL(baseURL);
      return httpClient
          .execute(
              buildDelete(
                  url, key, token, new CustomQueryParameter("url", targetURL.toExternalForm())))
          .thenApply(
              response -> {
                try {
                  return deserializeError(response);
                } catch (StreamException | IOException e) {
                  throw new CompletionException(e);
                }
              });
    } catch (MalformedURLException | URISyntaxException e) {
      throw new StreamException(e);
    }
  }

  public CompletableFuture<URL> process(Token token, URL targetURL, RequestOption option)
      throws StreamException {
    checkNotNull(targetURL, "No image to process");

    try {
      final URL url = buildImagesURL(baseURL);
      return httpClient
          .execute(
              buildGet(
                  url,
                  key,
                  token,
                  option,
                  new CustomQueryParameter("url", targetURL.toExternalForm())))
          .thenApply(
              response -> {
                try {
                  return deserialize(response, "file", URL.class);
                } catch (StreamException | IOException e) {
                  throw new CompletionException(e);
                }
              });
    } catch (MalformedURLException | URISyntaxException e) {
      throw new StreamException(e);
    }
  }
}
