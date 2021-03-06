package maldonado.indetect.models

import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.ArrayList

fun uniqueList(objectList: List<IClassifier.Recognition>): List<String>{
    val objects = ArrayList<String>()
    for (obj in objectList) {
        if(!objects.contains(obj.title)){
            objects.add(obj.title)
        }
    }

    return objects
}

fun animalList(objectList: List<IClassifier.Recognition>): List<IClassifier.Recognition>{
    val animals = ArrayList<IClassifier.Recognition>()
    val animalsT = listOf("Bird", "Cat", "Dog", "Horse", "Sheep", "Cow", "Elephant", "Bear", "Zebra", "Giraffe")
    for (obj in objectList) {
        if(animalsT.contains(obj.title)){
            animals.add(obj)
        }
    }

    return animals
}

fun loadDictionary(assetManager: AssetManager, filename: String): HashMap<String, String> {
    val dictionaryList = HashMap<String, String>()
    val reader = BufferedReader(InputStreamReader(assetManager.open(filename)))
    while (true) {
        val line = reader.readLine() ?: break
        val parts = line.split(":")
        dictionaryList[ parts[0] ] = parts[1]
    }
    reader.close()
    return dictionaryList
}

fun recognitionToString(recognitionList: List<IClassifier.Recognition>): String{
    var listString = ""
    for (obj in recognitionList) {
        listString += String.format("Title = %s (%.2f)\n", obj.title, obj.confidence)
    }

    return listString
}