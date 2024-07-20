class UtenteRegistrDTO {
  int id;
  String username;
  String email;
  String firstName;
  String lastName;
  String password;




  UtenteRegistrDTO({required this.id,required this.username,required this.email,required this.firstName,required this.lastName,required this.password});

  factory UtenteRegistrDTO.fromJson(Map<String, dynamic> json) {
    return UtenteRegistrDTO(
      id: json['id'],
      username: json['username'],
      email: json['email'],
      firstName: json['firstName'],
      lastName: json['lastName'],
      password: json['password']
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'username': username,
    'email':email,
    'firstName':firstName,
    'lastName':lastName,
    'password':password
  };

  @override
  String toString() {
    return 'UtenteRegistrDTO{username: $username, email: $email, firstName: $firstName, lastName: $lastName}';
  }
}