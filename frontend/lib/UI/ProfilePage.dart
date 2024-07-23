import 'package:flutter/material.dart';
import 'package:frontend/model/Model.dart';
import 'package:frontend/model/objects/Utente.dart';
import 'package:frontend/model/DTO/UtenteRegistrDTO.dart';
import 'package:frontend/model/support/LoginResult.dart';

class ProfilePage extends StatefulWidget {
  @override
  _ProfilePageState createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  Utente? _loggedUser;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _checkLoggedUser();
  }

  Future<void> _checkLoggedUser() async {
    Utente? user = await Model.sharedInstance.getLoggedUser();
    setState(() {
      _loggedUser = user;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
        title: Text('Profilo Utente'),
        backgroundColor: Colors.blueGrey.shade300,
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : _loggedUser != null
          ? _buildUserProfile()
          : _buildLoginRegister(),
    );
  }

  Widget _buildUserProfile() {
    return Padding(
      padding: const EdgeInsets.all(50.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildProfileDetail('Nome', _loggedUser!.nome),
          _buildProfileDetail('Cognome', _loggedUser!.cognome),
          _buildProfileDetail('Email', _loggedUser!.email),
          _buildProfileDetail('Punti Fedeltà', _loggedUser!.puntifedelta.toString()),
          SizedBox(height: 24),
          Center(
            child: ElevatedButton(
              onPressed: () async {
                await Model.sharedInstance.logOut();
                setState(() {
                  _loggedUser = null;
                });
              },
              child: Text('Logout',style: TextStyle(color: Colors.black)),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red, // Button color
                padding: EdgeInsets.symmetric(horizontal: 50, vertical: 15),
                textStyle: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProfileDetail(String title, String detail) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        children: [
          Expanded(
            child: Text(
              '$title:',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ),
          Expanded(
            child: Text(
              detail,
              style: TextStyle(fontSize: 18),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLoginRegister() {
    final _formKey = GlobalKey<FormState>();
    TextEditingController emailController = TextEditingController();
    TextEditingController passwordController = TextEditingController();

    return Center(
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 50.0),
        child: Card(
          elevation: 5,
          child: Padding(
            padding: const EdgeInsets.all(50.0),
            child:
            SizedBox(width: 350,
          child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  'Login',
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold,color: Colors.black),
                ),
                SizedBox(height: 20),
                Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      _buildTextFormField(
                        controller: emailController,
                        label: 'Email o username',
                        validator: (value) {
                          if (value == null || value.isEmpty) {
                            return 'Inserisci una email';
                          }
                          return null;
                        },
                      ),
                      SizedBox(height: 12),
                      _buildTextFormField(
                        controller: passwordController,
                        label: 'Password',
                        obscureText: true,
                        validator: (value) {
                          if (value == null || value.isEmpty) {
                            return 'Inserisci una password';
                          }
                          return null;
                        },
                      ),
                      SizedBox(height: 24),
                      ElevatedButton(
                        onPressed: () async {
                          if (_formKey.currentState!.validate()) {
                            var result = await Model.sharedInstance.logIn(
                              emailController.text,
                              passwordController.text,
                            );
                            if (result == LogInResult.logged) {
                              _checkLoggedUser();
                            } else {
                              showDialog(
                                context: context,
                                builder: (context) => AlertDialog(
                                  title: Text("Errore nel login "),
                                  content: Text("Credenziali errate o non valide. Riprova."),
                                  actions: [
                                    TextButton(
                                      onPressed: () {
                                        Navigator.of(context).pop();
                                      },
                                      child: Text("OK"),
                                    ),
                                  ],
                                ),
                              );

                            }
                          }
                        },
                        child: Text('Login',style: TextStyle(color: Colors.black)),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.blueGrey.shade300,
                          padding: EdgeInsets.symmetric(vertical: 15),
                          textStyle: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                      ),
                      SizedBox(height: 12),
                      GestureDetector(
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => RegistrationPage()),
                          );
                        },
                        child: Text(
                          'Non sei registrato? Registrati',
                          style: TextStyle(
                            color: Colors.blueGrey,
                            decoration: TextDecoration.underline,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
        ),),
    );
  }

  Widget _buildTextFormField({
    required TextEditingController controller,
    required String label,
    bool obscureText = false,
    required String? Function(String?) validator,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: TextFormField(
        controller: controller,
        decoration: InputDecoration(
          labelText: label,
          border: OutlineInputBorder(),
          contentPadding: EdgeInsets.symmetric(horizontal: 50, vertical: 12),
        ),
        obscureText: obscureText,
        validator: validator,
      ),
    );
  }
}

class RegistrationPage extends StatefulWidget {
  @override
  _RegistrationPageState createState() => _RegistrationPageState();
}

class _RegistrationPageState extends State<RegistrationPage> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _firstNameController = TextEditingController();
  final TextEditingController _lastNameController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Registrazione',style: TextStyle(color: Colors.black)),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
        backgroundColor: Colors.blueGrey.shade300,
      ),
      body: Center(
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 50.0),
          child: Card(
            elevation: 5,
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child:SizedBox(
    width: 350,
    child:
              Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    'Registrazione',
                    style: TextStyle(fontSize: 24,color: Colors.black),
                  ),
                  SizedBox(height: 20),
                  Form(
                    key: _formKey,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        _buildTextFormField(
                          controller: _usernameController,
                          label: 'Username',
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return 'Inserisci un username';
                            }
                            return null;
                          },
                        ),
                        SizedBox(height: 12),
                        _buildTextFormField(
                          controller: _emailController,
                          label: 'Email',
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return 'Inserisci una email';
                            }
                            return null;
                          },
                        ),
                        SizedBox(height: 12),
                        _buildTextFormField(
                          controller: _firstNameController,
                          label: 'Nome',
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return 'Inserisci il tuo nome';
                            }
                            return null;
                          },
                        ),
                        SizedBox(height: 12),
                        _buildTextFormField(
                          controller: _lastNameController,
                          label: 'Cognome',
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return 'Inserisci il tuo cognome';
                            }
                            return null;
                          },
                        ),
                        SizedBox(height: 12),
                        _buildTextFormField(
                          controller: _passwordController,
                          label: 'Password',
                          obscureText: true,
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return 'Inserisci una password';
                            }
                            return null;
                          },
                        ),
                        SizedBox(height: 24),
                        ElevatedButton(
                          onPressed: () async {
                            if (_formKey.currentState!.validate()) {
                              UtenteRegistrDTO user = UtenteRegistrDTO(
                                id: 0,
                                username: _usernameController.text,
                                email: _emailController.text,
                                firstName: _firstNameController.text,
                                lastName: _lastNameController.text,
                                password: _passwordController.text,
                              );

                              Utente? result = await Model.sharedInstance.addUser(user);

                              if (result != null) {
                                ScaffoldMessenger.of(context).showSnackBar(
                                  SnackBar(content: Text('Registrazione riuscita!')),
                                );
                                Navigator.pop(context); // Ritorna alla pagina di login
                              } else {
                                showDialog(
                                  context: context,
                                  builder: (context) => AlertDialog(
                                    title: Text("Errore nella registrazione "),
                                    content: Text("Dati errati o non validi. Riprova."),
                                    actions: [
                                      TextButton(
                                        onPressed: () {
                                          Navigator.of(context).pop();
                                        },
                                        child: Text("OK"),
                                      ),
                                    ],
                                  ),
                                );
                              }
                            }
                          },
                          child: Text('Registrati',style: TextStyle(color: Colors.black)),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.blueGrey.shade300,
                            padding: EdgeInsets.symmetric(vertical: 15),
                            textStyle: TextStyle(fontSize: 16,color: Colors.black),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
        ),),
    );
  }

  Widget _buildTextFormField({
    required TextEditingController controller,
    required String label,
    bool obscureText = false,
    required String? Function(String?) validator,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: TextFormField(
        controller: controller,
        decoration: InputDecoration(
          labelText: label,
          border: OutlineInputBorder(),
          contentPadding: EdgeInsets.symmetric(horizontal: 50, vertical: 12),
        ),
        obscureText: obscureText,
        validator: validator,
      ),
    );
  }
}
