
public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String regex = "^\\/intel\\/procfs\\/filesystem\\/((?!\\/).)*\\/(space_free|space_reserved|space_used|space_percent_free|space_percent_reserved|space_percent_used|inodes_free|inodes_reserved|inodes_used|inodes_percent_free|inodes_percent_reserved|inodes_percent_used|device_name|device_type)$";
		String pass = "/intel/procfs/filesystem/sys_fs_cgroup/inodes_used";
		
		if (pass.matches(regex)) {
			System.out.println("Yes");
		} else System.out.println("No");

	}

}
