
class DettaglioDto {
  int idProdotto;
  int quantita;
  double prezzoUnitario;




  DettaglioDto({required this.idProdotto,required this.quantita,required this.prezzoUnitario});

  factory DettaglioDto.fromJson(Map<String, dynamic> json) {
    return DettaglioDto(
        idProdotto: json['idProdotto'],
        quantita: json['quantita'],
        prezzoUnitario: json['prezzoUnitario']
    );
  }

  Map<String, dynamic> toJson() => {
    'idProdotto': idProdotto,
    'quantita': quantita,
    'prezzoUnitario':prezzoUnitario,
  };

  @override
  String toString() {
    return 'DettaglioDto{idProdotto: $idProdotto, quantita: $quantita, prezzoUnitario: $prezzoUnitario}';
  }
}