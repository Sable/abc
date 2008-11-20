<script type="text/javascript">
    function setCookie(name, value, lifespan, access_path) {
        var cookietext = name + "=" + escape(value);
        if (lifespan != null) {
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
</script>