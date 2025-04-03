Feature: Test Xray


	@POEI20252-518 @BrunoA
	Scenario: Connexion non réussi
		Given L'utilisateur est sur la page de connexion de SauceDemo
		    When Il saisit le nom d'utilisateur "standard_user"
		    And Il saisit le mot de passe "wrong_password"
		    And Il clique sur le bouton de connexion
		    Then Il doit voir le message d'erreur "Epic sadface: Username and password do not match any user in this service"
		
	@POEI20252-466 @BrunoA
	Scenario: Connexion réussi
		Given L'utilisateur est sur la page de connexion de SauceDemo
		    When Il saisit le nom d'utilisateur "standard_user"
		    And Il saisit le mot de passe "secret_sauce"
		    And Il clique sur le bouton de connexion
		    Then Il doit être redirigé vers la page d'accueil
		


