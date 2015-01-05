package com.mayer.framework.web.provider;

import android.content.Context;
import android.text.TextUtils;

import com.mayer.framework.web.Logger.Logger;
import com.mayer.framework.web.model.INetworkExchangeProtocol;
import com.mayer.framework.web.model.IRequest;
import com.mayer.framework.web.model.IResponse;
import com.mayer.framework.web.model.WebParameter;
import com.mayer.framework.web.model.rest.RESTRequest;
import com.mayer.framework.web.model.rest.RESTResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class RESTWebService implements INetworkExchangeProtocol<RESTRequest, RESTResult> {


    protected Context context;
    protected IRequest request;

    protected RESTWebService() {

    }

    public INetworkExchangeProtocol setContext(Context context) {
        this.context = context;
        return this;
    }

    @Override
    public void post(RESTRequest request, RESTResult result) throws IOException {
        result.updateWith(pushPost(request.path, request.body, request.headers, request.parameters), RESTResultHandler.ContentType.JSON);

        if (!result.isValid() || result.getResultCode() != HttpStatus.SC_OK)
            Logger.e("RESTWebService.post(): error result: code: " + result.getResultCode() + " reason: " + result.getErrorReason());
    }

    @Override
    public IResponse getResponse() {
        return null;
    }

    @Override
    public void load() {

    }

    public static InputStream getEntityStream(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();

        if (entity == null) {
            Logger.d("Response body was null. Returning a null input stream.");
            return null;
        }

        InputStream entityStream = entity.getContent();

        if (entityStream == null) {
            Logger.d("Response contents were null. Returning a null input stream.");
            return null;
        }

        return entityStream;
    }

    /**
     * Perform http get request with or without query parameters
     *
     * @param path
     * @param parameters
     * @return
     * @throws IOException
     */
    public static HttpResponse pushGet(String path, WebParameter... parameters) throws IOException {
        return pushGetWithQuery(path, parameters);
    }

    /**
     * Perform http post request with or without query parameters
     *
     * @param path
     * @param parameters
     * @return
     * @throws IOException
     */
    public static HttpResponse pushPost(String path, String body, Header[] headers, WebParameter... parameters) throws IOException {
        return pushPostWithQuery(path, body, headers, parameters);
    }

    /**
     * Perform http get request with query parameters
     *
     * @param path
     * @param queryParameters
     * @return
     * @throws IOException
     */
    public static HttpResponse pushGetWithQuery(String path,
                                                   WebParameter[] queryParameters) throws IOException {
        HttpGet get = createGet(path);
        return pushRequestWithExceptions(get, queryParameters);
    }

    /**
     * Perform http post request with query parameters
     *
     * @param path
     * @param queryParameters
     * @return
     * @throws IOException
     */
    public static HttpResponse pushPostWithQuery(String path,
                                                    String body,
                                                    Header[] headers,
                                                    WebParameter[] queryParameters) throws IOException {
        HttpPost get = createPost(path);
        if (headers != null && headers.length > 0){
            get.setHeaders(headers);
        }
        return pushRequest(get, body, queryParameters);
    }

    /**
     * Perform standard http request
     *
     * @param request
     * @param headers
     * @return
     */
    public static HttpResponse pushRequest(HttpEntityEnclosingRequestBase request, String entityBody, WebParameter[] headers) {
        if (request == null) {
            return null;
        }
        HttpResponse response = null;
        try {
            if (RESTResultHandler.D) {
                Logger.d("Attaching entity : %s", entityBody);
            }
            addEntity(request, entityBody);
            response = pushRequestWithExceptions(request, headers);
        } catch (SocketException e) {
            // already handled
        } catch (ClientProtocolException e) {
            // already handled
        } catch (IOException e) {
            // already handled
        }
        return response;
    }



    /**
     * Perform standard http request throwing various exceptions
     *
     * @param request
     * @param queryParameters
     * @return
     * @throws SocketException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static HttpResponse pushRequestWithExceptions(HttpRequestBase request,
                                                            WebParameter[] queryParameters) throws SocketException,
            ClientProtocolException,
            IOException {
        // protect against null requests.
        if (request == null) {
            return null;
        }
        try {
            // set the query parameters
            setQueryParameters(request, queryParameters);

            return executeRequest(request);
        } catch (SocketException ex) {
            Logger.e("Socket exception. Assuming offline.", ex);
            throw ex;
        } catch (ClientProtocolException ex) {
            Logger.e("ClientProtocolException. Are you using the correct protocol?", ex);
            throw ex;
        } catch (IOException ex) {
            Logger.e("IO exception. Do you have network access?", ex);
            throw ex;
        }
    }

    /**
     * Simply executes the request. Throws various errors.
     *
     * @param request
     * @return
     */
    public static HttpResponse executeRequest(HttpRequestBase request) throws SocketException,
            ClientProtocolException,
            IOException {
        HttpClient client = new DefaultHttpClient(); // get http client
        return client.execute(request);
    }

    /**
     * Create a get request for a given path.
     */
    public static HttpGet createGet(String path) {
        HttpGet get = null;
        try {
            // create the get here.
            get = new HttpGet(new URI(path));
        } catch (URISyntaxException ex) {
            Logger.e("URI syntax exception. Did you format your uri correctly?", ex);
        }
        return get;
    }


    /**
     * Create a post request for the given path, with the iven entity body and body parameters.
     */
    public static HttpPost createPost(String path){
        HttpPost post = null;
        try {
            // create the post here.
            post = new HttpPost(new URI(path));
        } catch (URISyntaxException ex) {
            Logger.e("URI syntax exception. Did you format your uri correctly?", ex);
        }
        return post;
    }

    /**
     * Used to set query parameters for any http requests.
     *
     * @param request
     *            - the request to append parameters to
     * @param parameters
     *            - the parameters to append.
     */
    public static void setQueryParameters(HttpRequestBase request,
                                             WebParameter[] parameters) {
        if (parameters != null && parameters.length != 0) {
            StringBuilder params = null;

            // addItemDiscount in the session id and sequence number

            // addItemDiscount the parameters, if there are any.

            for (WebParameter parameter : parameters) {

                if (params == null) {

                    params = new StringBuilder(request.getURI().toString());
                    params.append("?");
                } else {
                    params.append("&");
                }

                params.append(parameter.toString());
            }

            try {
                // return if you haven't done anything.
                if (params == null) {
                    return;
                }
                // else create a URI with the parameters
                Logger.d("The url to go : %s", params.toString());
                request.setURI(new URI(params.toString()));
            } catch (URISyntaxException ex) {
                Logger.e(String.format("There is an error in the uri: %s", params), ex);
            }
        }
    }

    /**
     * Utility method that creates a webservice URL from the path given.
     */
    public static String createPath(String baseUrl, String... paths) {

        StringBuilder completePath = new StringBuilder(baseUrl);

        for (String path : paths) {
            if (!TextUtils.isEmpty(path)) {
                completePath.append('/');
                completePath.append(path);
            }
        }

        return completePath.toString();
    }

    public static void addEntity(HttpEntityEnclosingRequest request, String entityBody) {

        try {
            if(entityBody!=null){
                // create the entity
                HttpEntity entity = new StringEntity(entityBody);
                request.setEntity(entity);
            }
        } catch(UnsupportedEncodingException ex){
            Logger.e(String.format("You are trying to encode the string %s incorrectly", entityBody), ex);
        }
    }
}
