package armadillo.result;

/**
 * HTTP REST API统一响应类
 */
public class ApiResponse {
    private int code;
    private String msg;
    private Object data;

    public ApiResponse() {
    }

    public ApiResponse(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ApiResponse success(Object data) {
        return new ApiResponse(200, "success", data);
    }

    public static ApiResponse success(String msg, Object data) {
        return new ApiResponse(200, msg, data);
    }

    public static ApiResponse error(int code, String msg) {
        return new ApiResponse(code, msg, null);
    }

    public static ApiResponse error(String msg) {
        return new ApiResponse(400, msg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
