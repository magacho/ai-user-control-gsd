# AI User Control - Project Guidelines

## Technical Stack Decisions

### No Lombok
- **Do NOT use Lombok** in any Java code (no `@Data`, `@Getter`, `@Setter`, `@Builder`, `@AllArgsConstructor`, etc.)
- Write explicit getters, setters, constructors, `equals()`, `hashCode()`, and `toString()` methods
- Rationale: code is generated via AI, so there is no productivity gain from annotation magic; explicit code is clearer and easier to review
- During Phase 1 execution: remove Lombok dependency from `pom.xml`, remove `lombok.config`, remove Lombok from `maven-compiler-plugin` annotation processors and `spring-boot-maven-plugin` excludes
