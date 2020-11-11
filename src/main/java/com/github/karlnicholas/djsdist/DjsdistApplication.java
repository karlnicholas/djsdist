package com.github.karlnicholas.djsdist;

import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DjsdistApplication implements ApplicationRunner {
	public static void main(String[] args) {
		SpringApplication.run(DjsdistApplication.class, args);
	}

	private final DataSource dataSource; 

	public DjsdistApplication(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		try ( Statement statement = dataSource.getConnection().createStatement() ) {
			statement.execute("create table business_date (id bigint identity, business_date date, processed_date date, primary key (id))");
			statement.execute("create table account (id bigint identity, open_date date, primary key (id))");
			statement.execute("create table account_closed (id bigint identity, open_date date, original_id bigint, primary key (id))");
			statement.execute("create table loan (id bigint identity, fixed_mindue decimal(19,2), inception_date date, interest_rate decimal(19,2), principal decimal(19,2), term_months integer, account_id bigint, primary key (id))");
			statement.execute("create table loan_closed (id bigint identity, fixed_mindue decimal(19,2), inception_date date, interest_rate decimal(19,2), principal decimal(19,2), term_months integer, account_closed_id bigint, primary key (id))");
			statement.execute("create table transaction_closed (id bigint identity, business_date date, payload varchar(4000), transaction_date date, transaction_type varchar(255), version bigint, account_closed_id bigint, primary key (id))");
			statement.execute("create table transaction_open (id bigint identity, business_date date, payload varchar(4000), transaction_date date, transaction_type varchar(255), version bigint, account_id bigint, primary key (id))");
			statement.execute("create table transaction_submitted (id bigint identity, business_date date, payload varchar(4000), transaction_date date, transaction_type varchar(255), version bigint, account_id bigint, primary key (id))");
			statement.execute("create table transaction_rejected (id bigint not null, business_date date, payload varchar(4000), transaction_date date, transaction_type varchar(255), version bigint, account_id bigint, primary key (id))");
			statement.execute("create table billing_cycle (id bigint identity, period_end_date date, business_date date, payload varchar(4000), transaction_date date, transaction_type varchar(255), version bigint, account_id bigint, primary key (id))");
			statement.execute("create index IDX8hl04kre9pgr0b9b7r5jipqra on account_closed (original_id)");
			statement.execute("create index IDXfjs1q0rt6k8jcu5gltousur8y on transaction_open (account_id, transaction_type)");
			statement.execute("alter table loan add constraint FKnbbh9l71cf3hk76mvmjjfn7n5 foreign key (account_id) references account");
			statement.execute("alter table loan_closed add constraint FKpgpt07xu8o7gp4p56up2rf2f8 foreign key (account_closed_id) references account_closed(id)");
			statement.execute("alter table transaction_closed add constraint FKevvq0jionbirjxljl0y736k53 foreign key (account_closed_id) references account_closed(id)");
			statement.execute("alter table transaction_open add constraint FKsy6domeeutsrpk8oe5vg15s2t foreign key (account_id) references account");
			statement.execute("alter table transaction_submitted add constraint FKq4kgxmb4wd1mfe4wj825drqks foreign key (account_id) references account");
/*
			ResultSet resultSet = statement.executeQuery("select * from business_date where id = (select max(id) business_date)");
			if (!resultSet.next()) {
				//if rs.next() returns false
                //then there are no rows.
				System.out.println("No records found");
			}
*/
		}

		System.out.println("Done DB Build");
	}
}
