alter table TERM_DIST_TEMP add index base_comp_ts (BASE_TERM, COMP_TERM, GRP_TS) ;

alter table TERM_DIST modify GRP_TS BIGINT ;

create index idx_base_comp_idx on term_dist
    (BASE_TERM, COMP_TERM, SEED_NO, TERM_NO) ;