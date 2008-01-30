//Listing 6.4 An aspect that detects public access to members

aspect DetectPublicAccessMembers {
    declare warning :
	get(public !final * *) || set(public * *) :
	"Please consider using nonpublic access";
}
