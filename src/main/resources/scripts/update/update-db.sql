-- update social services table
ALTER TABLE public.social_servicies
  ADD COLUMN service_type BIGINT NOT NULL,
  ADD CONSTRAINT service_uk UNIQUE (service_type);


-- create assignObjects table
CREATE TABLE public.assignObjects (
  policy_id BIGINT NOT NULL,
  object_id BIGINT NOT NULL,
  CONSTRAINT assignments_pk PRIMARY KEY (policy_id, object_id)
);

ALTER TABLE public.assignObjects ADD CONSTRAINT assignments_policies_fk
FOREIGN KEY (policy_id)
REFERENCES public.policies (policy_id)
ON DELETE CASCADE
ON UPDATE CASCADE
DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE public.assignObjects ADD CONSTRAINT assignments_objects_fk
FOREIGN KEY (object_id)
REFERENCES public.objects (smart_object_id)
ON DELETE CASCADE
ON UPDATE CASCADE
DEFERRABLE INITIALLY IMMEDIATE;


-- update conditions
ALTER TABLE public.conditions
  RENAME COLUMN condition_id TO node_id;

ALTER TABLE public.conditions
  RENAME COLUMN next_condition_id TO parent_node_id;

ALTER TABLE public.conditions
  ALTER COLUMN type_id DROP NOT NULL;

ALTER SEQUENCE public.conditions_condition_id_seq RENAME TO conditions_node_id_seq;