package com.kaching123.tcr.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.payment.blackstone.payment.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.request.AutomaticBatchCloseRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.ClosePreauthRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoFullRefundRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoSettlementRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoVoidRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.ProcessPreauthRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.RefundRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.SaleRequest;
import com.kaching123.tcr.model.payment.general.transaction.TransactionType;
import com.kaching123.tcr.websvc.WebAPI;
import com.kaching123.tcr.websvc.WebAPI.BlackStoneAPI;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map.Entry;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class GSON {

    private static GSON ourInstance = new GSON();
    private Gson instance;

    public static GSON getInstance() {
        return ourInstance;
    }

    private GSON() {
        instance = create();
    }

    private final Gson create() {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(TransactionType.class, new TTJsonSerializer());
        gson.registerTypeAdapter(TransactionType.class, new TTJsondeserializer());
        gson.registerTypeAdapter(TransactionStatusCode.class, new RCJsondeserializer());
        gson.excludeFieldsWithoutExposeAnnotation();
        gson.registerTypeAdapterFactory(new SaleAdapterFactory());
        gson.registerTypeAdapterFactory(new VoidAdapterFactory());
        gson.registerTypeAdapterFactory(new FullRefundAdapterFactory());
        gson.registerTypeAdapterFactory(new RefundAdapterFactory());
        gson.registerTypeAdapterFactory(new ProcessPreauthAdapterFactory());
        gson.registerTypeAdapterFactory(new ClosePreauthAdapterFactory());
        gson.registerTypeAdapterFactory(new DoSettlementAdapterFactory());
        gson.registerTypeAdapterFactory(new AutomaticBatchCloseAdapterFactory());
        gson.setPrettyPrinting();
        return gson.create();
    }

    public Gson getGson() {
        return instance;
    }

    private class RCJsondeserializer implements JsonDeserializer<TransactionStatusCode> {

        @Override
        public TransactionStatusCode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return TransactionStatusCode.valueOf(jsonElement.getAsInt());
        }
    }

    private class TTJsondeserializer implements JsonDeserializer<TransactionType> {

        @Override
        public TransactionType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return TransactionType.valueOf(jsonElement.getAsInt());
        }
    }

    private class TTJsonSerializer implements JsonSerializer<TransactionType> {

        @Override
        public JsonElement serialize(TransactionType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.value());
        }
    }

    private class SaleAdapterFactory extends CustomizedTypeAdapterFactory<SaleRequest> {

        private SaleAdapterFactory() {
            super(SaleRequest.class);
        }

        @Override protected void beforeWrite(SaleRequest source, JsonElement toSerialize) {
            JsonObject body = toSerialize.getAsJsonObject();
            consume(body, SaleRequest.GSON_ATTR_USER);
            consume(body, SaleRequest.GSON_ATTR_TRANSACTION);
            consume(body, SaleRequest.GSON_ATTR_CARD);
        }
    }

    private class VoidAdapterFactory extends CustomizedTypeAdapterFactory<DoVoidRequest> {

        private VoidAdapterFactory() {
            super(DoVoidRequest.class);
        }

        @Override protected void beforeWrite(DoVoidRequest source, JsonElement toSerialize) {
            JsonObject body = toSerialize.getAsJsonObject();
            consume(body, SaleRequest.GSON_ATTR_USER);
            consume(body, SaleRequest.GSON_ATTR_TRANSACTION);
        }
    }

    private class FullRefundAdapterFactory extends CustomizedTypeAdapterFactory<DoFullRefundRequest> {

        private FullRefundAdapterFactory() {
            super(DoFullRefundRequest.class);
        }

        @Override protected void beforeWrite(DoFullRefundRequest source, JsonElement toSerialize) {
            JsonObject body = toSerialize.getAsJsonObject();
            consume(body, SaleRequest.GSON_ATTR_USER);
            consume(body, SaleRequest.GSON_ATTR_TRANSACTION);
        }
    }

    private class RefundAdapterFactory extends CustomizedTypeAdapterFactory<RefundRequest> {

        private RefundAdapterFactory() {
            super(RefundRequest.class);
        }

        @Override protected void beforeWrite(RefundRequest source, JsonElement toSerialize) {
            JsonObject body = toSerialize.getAsJsonObject();
            consume(body, RefundRequest.GSON_ATTR_USER);
            consume(body, RefundRequest.GSON_ATTR_TRANSACTION);
            consume(body, RefundRequest.GSON_ATTR_CARD);
            swapKeys(body, WebAPI.BlackStoneAPI.REQUEST_PARAM_TRACK2, BlackStoneAPI.REQUEST_PARAM_TRACKDATA);
        }
    }

    private class ProcessPreauthAdapterFactory extends CustomizedTypeAdapterFactory<ProcessPreauthRequest> {

        private ProcessPreauthAdapterFactory() {
            super(ProcessPreauthRequest.class);
        }

        @Override protected void beforeWrite(ProcessPreauthRequest source, JsonElement toSerialize) {
            JsonObject body = toSerialize.getAsJsonObject();
            consume(body, RequestBase.GSON_ATTR_USER);
            consume(body, RequestBase.GSON_ATTR_TRANSACTION);
            consume(body, RequestBase.GSON_ATTR_CARD);
            removeKey(body, BlackStoneAPI.REQUEST_PARAM_TRANSACTIONTYPE);
            swapKeys(body, BlackStoneAPI.REQUEST_PARAM_TRACK2, BlackStoneAPI.REQUEST_PARAM_TRACKDATA);
            swapKeys(body, BlackStoneAPI.REQUEST_PARAM_CARDNUMBER, BlackStoneAPI.REQUEST_PARAM_ACCOUNT);
            swapKeys(body, BlackStoneAPI.REQUEST_PARAM_CVN, BlackStoneAPI.REQUEST_PARAM_CV);
        }
    }

    private class ClosePreauthAdapterFactory extends CustomizedTypeAdapterFactory<ClosePreauthRequest> {

        private ClosePreauthAdapterFactory() {
            super(ClosePreauthRequest.class);
        }

        @Override protected void beforeWrite(ClosePreauthRequest source, JsonElement toSerialize) {
            JsonObject body = toSerialize.getAsJsonObject();
            consume(body, RequestBase.GSON_ATTR_USER);
            consume(body, RequestBase.GSON_ATTR_TRANSACTION);
            removeKey(body, BlackStoneAPI.REQUEST_PARAM_TRANSACTIONTYPE);
            swapKeys(body, BlackStoneAPI.REQUEST_PARAM_SERVICETRANSACTIONNUMBER, BlackStoneAPI.RESULT_PARAM_SERVICEREFERENCENUMBER);
        }
    }

    private class DoSettlementAdapterFactory extends CustomizedTypeAdapterFactory<DoSettlementRequest> {

        private DoSettlementAdapterFactory() {
            super(DoSettlementRequest.class);
        }

        @Override protected void beforeWrite(DoSettlementRequest source, JsonElement toSerialize) {
            JsonObject body = toSerialize.getAsJsonObject();
            consume(body, RequestBase.GSON_ATTR_USER);
        }
    }


    private class AutomaticBatchCloseAdapterFactory extends CustomizedTypeAdapterFactory<AutomaticBatchCloseRequest> {

        private AutomaticBatchCloseAdapterFactory() {
            super(AutomaticBatchCloseRequest.class);
        }

        @Override protected void beforeWrite(AutomaticBatchCloseRequest source, JsonElement toSerialize) {
            JsonObject body = toSerialize.getAsJsonObject();
            consume(body, RequestBase.GSON_ATTR_USER);
        }
    }

    private abstract class CustomizedTypeAdapterFactory<C> implements TypeAdapterFactory {
        private final Class<C> customizedClass;

        protected void removeKey(JsonObject master, String key) {
            master.remove(key);
        }

        protected void swapKeys(JsonObject master, String from, String to) {
            if (master.has(from)) {
                master.add(to, master.remove(from));
            }
        }

        protected void consume(JsonObject master, String what) {
            if (master.has(what)) {
                JsonObject user =  master.getAsJsonObject(what);
                master.remove(what);

                for (Entry<String, JsonElement> entry : user.entrySet()) {
                    boolean primitive = entry.getValue().isJsonPrimitive();
                    if (primitive) {
                        master.add(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                Logger.d("Nothing to consume");
            }
        }

        public CustomizedTypeAdapterFactory(Class<C> customizedClass) {
            this.customizedClass = customizedClass;
        }

        @SuppressWarnings("unchecked") // we use a runtime check to guarantee that 'C' and 'T' are equal
        public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return type.getRawType() == customizedClass
                    ? (TypeAdapter<T>) customizeMyClassAdapter(gson, (TypeToken<C>) type)
                    : null;
        }

        private TypeAdapter<C> customizeMyClassAdapter(Gson gson, TypeToken<C> type) {

            final TypeAdapter<C> delegate = gson.getDelegateAdapter(this, type);
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

            return new TypeAdapter<C>() {

                @Override public void write(JsonWriter out, C value) throws IOException {
                    JsonElement tree = delegate.toJsonTree(value);
                    beforeWrite(value, tree);
                    elementAdapter.write(out, tree);
                }
                @Override public C read(JsonReader in) throws IOException {
                    JsonElement tree = elementAdapter.read(in);
                    afterRead(tree);
                    return delegate.fromJsonTree(tree);
                }
            };
        }

        protected void beforeWrite(C source, JsonElement toSerialize) {
        }

        protected void afterRead(JsonElement deserialized) {
        }
    }
}
