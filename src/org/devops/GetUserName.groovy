// define GetUserName
def call(users,id){
	for (i in users){
		if (i["id"] == id){
			return i["name"]
		}
	}
	return "null"
}