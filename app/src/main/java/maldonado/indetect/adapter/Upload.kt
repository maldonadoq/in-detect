package maldonado.indetect.adapter

class Upload(_name: String, _url: String, _key: String) {
    private var name: String = _name
    private var url: String = _url
    private var key: String = _key

    fun getName(): String{
        return name
    }

    fun getUrl(): String{
        return url
    }

    fun getKey(): String{
        return key
    }
}