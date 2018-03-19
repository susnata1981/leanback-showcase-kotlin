package android.support.v17.leanback.supportleanbackshowcase

/**
 * Created by susnata on 3/17/18.
 */
class Main {

    fun main(args: Array<String>) {
        val m = mutableMapOf<Integer, MutableList<Integer>>();
        m.put(Integer(0), mutableListOf((Integer(1)), Integer(2), Integer(3)));

        val l = listOf<Integer>(Integer(4), Integer(5));
        m.get(Integer(0))!!.addAll(l);
        println(m);
    }
}
