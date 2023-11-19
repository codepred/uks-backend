package codepred.whitelist;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "whitelist")
@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Whitelist {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Whitelist.seq")
    @SequenceGenerator(name = "Whitelist.seq", sequenceName = "SEQ_WHITELIST_ID", allocationSize = 1)
    private Integer id;

    @Column(name = "email", unique = true)
    private String email;
}