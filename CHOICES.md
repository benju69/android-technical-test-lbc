# Justification des choix techniques

## 1. Injection de dépendances - Hilt

**Choix** : Utilisation de **Hilt** comme framework d'injection de dépendances.

**Justification** :
- **Standard Android** : Framework officiel recommandé par Google, construit sur Dagger 2
- **Boilerplate réduit** : Élimine le code Factory pour les ViewModels grâce à `@HiltViewModel`
- **Type-safety** : Détection des erreurs de dépendances à la compilation
- **Scopes automatiques** : Gestion native des cycles de vie (Singleton, ViewModel, Activity)
- **Testabilité** : Facilite le remplacement des dépendances en tests unitaires
- **Performance** : Génération de code à la compilation sans surcharge runtime
- **Maintenabilité** : Architecture claire avec séparation modules métier (NetworkModule, RepositoryModule)

**Implémentation** :
- Modules créés : `NetworkModule` (Retrofit, OkHttp), `RepositoryModule` (repositories)
- ViewModels injectés automatiquement via `@HiltViewModel`, ils reçoivent automatiquement leurs dépendances
- Dépendances fournies avec `@Provides` dans les modules appropriés
- Utilisation de `@Singleton` pour les instances uniques
- Utilisation de `@Inject` pour l'injection des dépendances
- Utilisation de `@AndroidEntryPoint` pour les points d'entrée d'Android (Activities/Fragment)
