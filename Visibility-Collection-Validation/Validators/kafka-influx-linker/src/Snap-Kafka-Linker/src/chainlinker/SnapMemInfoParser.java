package chainlinker;

/*
 * Corresponding to snap-plugin-collector-meminfo
 * 
 * CAUTION: This setting may fail in case if the plugins' version mismatch with the below.
 * - collector:meminfo:3
 */
public class SnapMemInfoParser extends SnapPluginParser {
	public SnapMemInfoParser() {
		super();
		// All these data forms must be updated when snap publisher's output format is changed.
		
		// long value types
		typeMap.put("/intel/procfs/meminfo/mem_total", lClass);
		typeMap.put("/intel/procfs/meminfo/mem_used", lClass);
		typeMap.put("/intel/procfs/meminfo/mem_free", lClass);
		typeMap.put("/intel/procfs/meminfo/mem_available", lClass);
		typeMap.put("/intel/procfs/meminfo/buffers", lClass);
		typeMap.put("/intel/procfs/meminfo/cached", lClass);
		typeMap.put("/intel/procfs/meminfo/swap_cached", lClass);
		typeMap.put("/intel/procfs/meminfo/active", lClass);
		typeMap.put("/intel/procfs/meminfo/inactive", lClass);
		typeMap.put("/intel/procfs/meminfo/active_anon", lClass);
		typeMap.put("/intel/procfs/meminfo/inactive_anon", lClass);
		typeMap.put("/intel/procfs/meminfo/active_file", lClass);
		typeMap.put("/intel/procfs/meminfo/inactive_file", lClass);
		typeMap.put("/intel/procfs/meminfo/unevictable", lClass);
		typeMap.put("/intel/procfs/meminfo/mlocked", lClass);
		typeMap.put("/intel/procfs/meminfo/high_total", lClass);
		typeMap.put("/intel/procfs/meminfo/high_free", lClass);
		typeMap.put("/intel/procfs/meminfo/low_total", lClass);
		typeMap.put("/intel/procfs/meminfo/low_free", lClass);
		typeMap.put("/intel/procfs/meminfo/mmap_copy", lClass);
		typeMap.put("/intel/procfs/meminfo/swap_total", lClass);
		typeMap.put("/intel/procfs/meminfo/swap_free", lClass);
		typeMap.put("/intel/procfs/meminfo/dirty", lClass);
		typeMap.put("/intel/procfs/meminfo/writeback", lClass);
		typeMap.put("/intel/procfs/meminfo/anon_pages", lClass);
		typeMap.put("/intel/procfs/meminfo/mapped", lClass);
		typeMap.put("/intel/procfs/meminfo/shmem", lClass);
		typeMap.put("/intel/procfs/meminfo/slab", lClass);
		typeMap.put("/intel/procfs/meminfo/sreclaimable", lClass);
		typeMap.put("/intel/procfs/meminfo/sunreclaim", lClass);
		typeMap.put("/intel/procfs/meminfo/kernel_stack", lClass);
		typeMap.put("/intel/procfs/meminfo/page_tables", lClass);
		typeMap.put("/intel/procfs/meminfo/quicklists", lClass);
		typeMap.put("/intel/procfs/meminfo/nfs_unstable", lClass);
		typeMap.put("/intel/procfs/meminfo/bounce", lClass);
		typeMap.put("/intel/procfs/meminfo/writeback_tmp", lClass);
		typeMap.put("/intel/procfs/meminfo/commit_limit", lClass);
		typeMap.put("/intel/procfs/meminfo/committed_as", lClass);
		typeMap.put("/intel/procfs/meminfo/vmalloc_total", lClass);
		typeMap.put("/intel/procfs/meminfo/vmalloc_used", lClass);
		typeMap.put("/intel/procfs/meminfo/vmalloc_chunk", lClass);
		typeMap.put("/intel/procfs/meminfo/hardware_corrupted", lClass);
		typeMap.put("/intel/procfs/meminfo/anon_huge_pages", lClass);
		typeMap.put("/intel/procfs/meminfo/cma_total", lClass);
		typeMap.put("/intel/procfs/meminfo/cma_free", lClass);
		typeMap.put("/intel/procfs/meminfo/huge_pages_total", lClass);
		typeMap.put("/intel/procfs/meminfo/huge_pages_free", lClass);
		typeMap.put("/intel/procfs/meminfo/huge_pages_rsvd", lClass);
		typeMap.put("/intel/procfs/meminfo/huge_pages_surp", lClass);
		typeMap.put("/intel/procfs/meminfo/hugepagesize", lClass);
		typeMap.put("/intel/procfs/meminfo/direct_map4k", lClass);
		typeMap.put("/intel/procfs/meminfo/direct_map4m", lClass);
		typeMap.put("/intel/procfs/meminfo/direct_map2m", lClass);
		typeMap.put("/intel/procfs/meminfo/direct_map1g", lClass);
		
		// double value types
		typeMap.put("/intel/procfs/meminfo/mem_total_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/mem_used_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/mem_free_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/mem_available_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/buffers_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/cached_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/swap_cached_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/active_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/inactive_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/active_anon_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/inactive_anon_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/active_file_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/inactive_file_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/unevictable_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/mlocked_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/high_total_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/high_free_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/low_total_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/low_free_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/mmap_copy_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/swap_total_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/swap_free_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/dirty_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/writeback_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/anon_pages_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/mapped_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/shmem_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/slab_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/sreclaimable_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/sunreclaim_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/kernel_stack_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/page_tables_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/quicklists_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/nfs_unstable_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/bounce_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/writeback_tmp_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/commit_limit_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/committed_as_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/vmalloc_total_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/vmalloc_used_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/vmalloc_chunk_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/hardware_corrupted_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/anon_huge_pages_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/cma_total_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/cma_free_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/huge_pages_total_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/huge_pages_free_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/huge_pages_rsvd_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/huge_pages_surp_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/hugepagesize_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/direct_map4k_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/direct_map4m_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/direct_map2m_perc", lfClass);
		typeMap.put("/intel/procfs/meminfo/direct_map1g_perc", lfClass);		
	}	
}
