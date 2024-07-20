
class Utente {
  int id;
  String nome;
  String cognome;
  String email;
  int puntifedelta;
  // Carrello carrello;




  Utente({required this.id,required this.nome,required this.cognome,required this.email,required this.puntifedelta});

  factory Utente.fromJson(Map<String, dynamic> json) {
    return Utente(
        id: json['id'],
        nome: json['nome'],
        cognome: json['cognome'],
        email: json['email'],
        puntifedelta: json['puntifedelta'],
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'nome': nome,
    'cognome':cognome,
    'email':email,
    'puntifedelta':puntifedelta,
  };

  @override
  String toString() {
    return 'Utente{nome: $nome, cognome: $cognome, email: $email, puntifedelta: $puntifedelta}';
  }
}