package running.domain;

/**
 * Athletics category
 */
public class Category {
	private int id;
	private String name;
	private int sex;
	private int startYear;
	private int endYear;
	public static final int MALE = 1;
	public static final int FEMALE = 2;
	private static final Category[] cats = {
			new Category(1, "Juvenil Masculino", MALE, 1994, 1993),
			new Category(2, "Juvenil Femenino", FEMALE, 1994, 1993),
			new Category(3, "Junior Masculino", MALE, 1992, 1991),
			new Category(4, "Junior Femenino", FEMALE, 1992, 1991),
			new Category(5, "Promesa Masculino", MALE, 1990, 1988),
			new Category(6, "Promesa Femenino", FEMALE, 1990, 1988),
			new Category(7, "Senior Masculino", MALE, 1987, 1976),
			new Category(8, "Senior Femenino", FEMALE, 1987, 1976),
			new Category(9, "Veterano Masculino", MALE, 1975, 0),
			new Category(10, "Veterano Femenino", FEMALE, 1975, 0) };

	public Category(int id, String name, int sex, int startYear, int endYear) {
		this.id = id;
		this.name = name;
		this.sex = sex;
		this.startYear = startYear;
		this.endYear = endYear;
	}

	public static Category getCategory(int id) {
		return cats[id - 1];
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getStartYear() {
		return startYear;
	}

	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}

	public int getEndYear() {
		return endYear;
	}

	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}

	public String getYearsString() {
		if (startYear > 0 && endYear > 0) {
			return "Nacidos entre " + startYear + " y " + endYear;
		} else if (startYear > 0 && endYear == 0) {
			return "Nacidos en " + startYear + " o mayores";
		} else if (startYear == 0 && endYear > 0) {
			return "Nacidos en " + endYear + " o menores";
		} else if (startYear == 0 && endYear == 0) {
			return "Todos";
		}
		return "Ninguno";
	}
}
