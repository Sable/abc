function setCookie(name, value, lifespan, access_path) {
    var cookietext = name + "=" + escape(value);
    if (lifespan == null) {
    } else {
        var today = new Date();
        var expiredate = new Date();
        expiredate.setTime(today.getTime() + 1000 * 60 * 60 * 24 * lifespan);
        cookietext += "; expires=" + expiredate.toGMTString();
    }
    if (access_path != null) {
        cookietext += "; PATH=" + access_path;
    }
    document.cookie = cookietext;
    return null;
}

function setDatedCookie(name, value, expire, access_path) {
    var cookietext = name + "=" + escape(value)
            + ((expire == null) ? "" : ("; expires=" + expire.toGMTString()));
    if (access_path != null) {
        cookietext += "; PATH=" + access_path;
    }
    document.cookie = cookietext;
    return null;
}