class Prodotto {
  int id;
  String nome;
  String categoria;
  String descrizione;
  double prezzo;
  int quantita;
  int nascosto;


  Prodotto({required this.id, required this.nome, required this.categoria, required this.descrizione, required this.prezzo,required this.quantita,required this.nascosto});

  factory Prodotto.fromJson(Map<String, dynamic> json) {
    return Prodotto(
      id: json['id'],
      nome: json['nome'],
      descrizione: json['descrizione'],
      categoria: json['categoria'],
      quantita: json['quantita'],
      prezzo: json['prezzo'],
      nascosto: json['nascosto']
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'nome': nome,
    'descrizione': descrizione,
    'categoria':categoria,
    'quantita': quantita,
    'prezzo': prezzo,
    'nascosto': nascosto
  };

  @override
  String toString() {
    return nome;
  }


}