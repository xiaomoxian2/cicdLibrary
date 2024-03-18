package org.devops

// define GetUserName
def GetUserNameByID(users,id){
	for (i in users){
		if (i["id"] == id){
			return i["name"]
		}
	}
	return "null"
}