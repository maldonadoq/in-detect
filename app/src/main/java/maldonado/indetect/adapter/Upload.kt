package maldonado.indetect.adapter

class Upload(_name: String, _url: String) {
    private var name: String = _name
    private var url: String = _url

    fun getName(): String{
        return name
    }

    fun getUrl(): String{
        return url
    }
}