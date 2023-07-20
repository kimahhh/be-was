package controller;

import exception.BadRequestException;
import http.*;
import service.UserService;
import view.Page;

import java.util.HashMap;
import java.util.Map;

import static exception.ExceptionName.INVALID_URI;
import static exception.ExceptionName.NOT_ENOUGH_USER_INFORMATION;
import static http.Extension.HTML;
import static http.HttpMethod.POST;
import static utils.FileIOUtils.*;

public class UserController extends Controller {
    private final UserService userService = new UserService();
    private final Page page = new Page();

    public HttpResponse.ResponseBuilder loadFileByRequest(HttpRequest httpRequest) {
        try {
            if (httpRequest.getMethod().equals(POST)) {
                return routeByUriWithBody(httpRequest);
            }
            String uri = httpRequest.getUri();
            if (uri.contains("?")) {
                return routeByUriWithQuestion(uri);
            }
            String[] uris = uri.split("\\.");
            String extension = uris[uris.length - 1];
            if (MIME.getMIME().entrySet().stream().noneMatch(entry -> entry.getKey().equals(extension))) {
                return loadTemplatesFromPath(HttpStatus.NOT_FOUND, "/wrong_access.html");
            }
            if (extension.equals(HTML)) {
                return loadTemplatesFromPath(HttpStatus.OK, uri)
                        .setContentType(MIME.getMIME().get(HTML));
            }
            return loadStaticFromPath(HttpStatus.OK, uri)
                    .setContentType(MIME.getMIME().get(extension));
        } catch (Exception e) {
            String errorPage = page.getErrorPage(e.getMessage());
            return loadErrorFromPath(HttpStatus.NOT_FOUND, errorPage)
                    .setContentType(MIME.getMIME().get(HTML));
        }

    }

    private HttpResponse.ResponseBuilder routeByUriWithQuestion(String uri) {
        String[] apis = uri.split("\\?");
        if (apis[0].equals("/user/create")) {
            return createUser(parseParams(apis[1]));
        }
        throw new BadRequestException(INVALID_URI);
    }

    private HttpResponse.ResponseBuilder routeByUriWithBody(HttpRequest httpRequest) {
        String uri = httpRequest.getUri();
        if (uri.equals("/user/create")) {
            return createUser(parseParams(httpRequest.getBody()));
        }
        throw new BadRequestException(INVALID_URI);
    }

    private Map<String, String> parseParams(String parameter) {
        String[] params = parameter.split("&");
        Map<String, String> information = new HashMap<>();
        for (String param : params) {
            String[] info = param.split("=");
            if (info.length != 2)
                throw new BadRequestException(NOT_ENOUGH_USER_INFORMATION);
            information.put(info[0], info[1]);
        }
        return information;
    }

    private HttpResponse.ResponseBuilder createUser(Map<String, String> parameters) {
        userService.createUser(parameters);
        return loadTemplatesFromPath(HttpStatus.FOUND, "/index.html");
    }

}
