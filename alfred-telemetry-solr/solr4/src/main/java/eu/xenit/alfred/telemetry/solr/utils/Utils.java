package eu.xenit.alfred.telemetry.solr.utils;

public class Utils {
    public static String getMappingJmxMicrometer(String resp) {
        /* Heap */
        if(resp.equals("java_lang_Memory_HeapMemoryUsage_used"))
            return "jvm_memory_used_bytes{area=\"heap\"}";
        if(resp.equals("java_lang_Memory_HeapMemoryUsage_committed"))
            return "jvm_memory_committed_bytes{area=\"heap\"}";
        if(resp.equals("java_lang_Memory_HeapMemoryUsage_max"))
            return "jvm_memory_max_bytes{area=\"heap\"}";
        /* Non-heap */
        if(resp.equals("java_lang_Memory_NonHeapMemoryUsage_used"))
            return "jvm_memory_used_bytes{area=\"nonheap\"}";
        if(resp.equals("java_lang_Memory_NonHeapMemoryUsage_committed"))
            return "jvm_memory_committed_bytes{area=\"nonheap\"}";
        if(resp.equals("java_lang_Memory_NonHeapMemoryUsage_max"))
            return "jvm_memory_max_bytes{area=\"nonheap\"}";

        return null;
    }
}
