
public class test {

	public test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String test = "/intel/procfs/processes/kworker.3:0H/ps_count";
		if (test.matches("^\\/intel\\/procfs\\/processes\\/([0-9]|[a-z]|[A-Z]|_|\\-|:|\\.)*\\/(ps_vm|ps_rss|ps_data|ps_code|ps_stacksize|ps_cputime_user|ps_cputime_system|ps_pagefaults_min|ps_pagefaults_maj|ps_disk_ops_syscr|ps_disk_ops_syscw|ps_disk_octets_rchar|ps_disk_octets_wchar|ps_count)$")) {
			System.out.println("Yes");
		} else System.out.println("No");	
	}
}

