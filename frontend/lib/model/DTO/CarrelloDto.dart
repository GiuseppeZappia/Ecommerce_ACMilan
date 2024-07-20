import 'package:frontend/model/DTO/DettaglioDto.dart';

class CarrelloDto {
  List<DettaglioDto> listaDettagli;
  int idUtente;




  CarrelloDto({required this.listaDettagli,required this.idUtente});

  factory CarrelloDto.fromJson(Map<String, dynamic> json) {
    return CarrelloDto(
        listaDettagli: json['listaDettagli'],
        idUtente: json['idUtente']
    );
  }

  Map<String, dynamic> toJson() => {
    'listaDettagli': listaDettagli,
    'idUtente':idUtente,
  };

  @override
  String toString() {
    return 'CarrelloDto{listaDettagli: $listaDettagli,idUtente: $idUtente}';
  }
}