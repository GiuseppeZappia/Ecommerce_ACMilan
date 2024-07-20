class Carrello {
  int id;
  int attivo;



  Carrello({required this.id,required this.attivo});

  factory Carrello.fromJson(Map<String, dynamic> json) {
    return Carrello(
        id: json['id'],
        attivo: json['attivo']
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'attivo': attivo,
  };

  @override
  String toString() {
    return 'Carrello{id: $id, attivo: $attivo}';
  }
}