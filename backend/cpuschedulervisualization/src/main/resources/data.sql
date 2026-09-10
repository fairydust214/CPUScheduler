INSERT IGNORE INTO scenarios (
    id,
    name
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-000000000005'),
    'PCP Preemption and Priority Inheritance'
);

INSERT IGNORE INTO resources (
    id,
    name,
    priority_ceiling,
    scenario_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a301'),
    'R1',
    1,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000005')
);

INSERT IGNORE INTO tasks (
    id,
    name,
    arrival_time,
    duration,
    deadline,
    priority,
    scenario_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b301'),
    'T1',
    0,
    8,
    20,
    3,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000005')
);

INSERT IGNORE INTO tasks (
    id,
    name,
    arrival_time,
    duration,
    deadline,
    priority,
    scenario_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b302'),
    'T2',
    2,
    3,
    15,
    1,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000005')
);

INSERT IGNORE INTO tasks (
    id,
    name,
    arrival_time,
    duration,
    deadline,
    priority,
    scenario_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b303'),
    'T3',
    20,
    2,
    30,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000005')
);

INSERT IGNORE INTO resource_requests (
    id,
    name,
    start_offset,
    duration,
    task_id,
    resource_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c301'),
    'T1→R1',
    0,
    8,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b301'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a301')
);

INSERT IGNORE INTO resource_requests (
    id,
    name,
    start_offset,
    duration,
    task_id,
    resource_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c302'),
    'T2→R1',
    0,
    3,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b302'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a301')
);

INSERT IGNORE INTO scenarios (
    id,
    name
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-000000000006'),
    'PCP Showcase - Preemption, Blocking, Inheritance, Multiple Resources'
);

INSERT IGNORE INTO resources (
    id,
    name,
    priority_ceiling,
    scenario_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a601'),
    'R1',
    3,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000006')
);

INSERT IGNORE INTO resources (
    id,
    name,
    priority_ceiling,
    scenario_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a602'),
    'R2',
    3,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000006')
);

INSERT IGNORE INTO resources (
    id,
    name,
    priority_ceiling,
    scenario_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a603'),
    'R3',
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000006')
);

INSERT IGNORE INTO tasks (
    id,
    name,
    arrival_time,
    duration,
    deadline,
    priority,
    scenario_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b601'),
    'T1',
    0,
    9,
    40,
    1,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000006')
);

INSERT IGNORE INTO tasks (
    id,
    name,
    arrival_time,
    duration,
    deadline,
    priority,
    scenario_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b602'),
    'T2',
    2,
    4,
    40,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000006')
);

INSERT IGNORE INTO tasks (
    id,
    name,
    arrival_time,
    duration,
    deadline,
    priority,
    scenario_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b603'),
    'T3',
    5,
    4,
    40,
    3,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000006')
);

INSERT IGNORE INTO resource_requests (
    id,
    name,
    start_offset,
    duration,
    task_id,
    resource_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c601'),
    'T1->R1',
    1,
    6,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b601'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a601')
);

INSERT IGNORE INTO resource_requests (
    id,
    name,
    start_offset,
    duration,
    task_id,
    resource_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c602'),
    'T1->R2',
    2,
    3,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b601'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a602')
);

INSERT IGNORE INTO resource_requests (
    id,
    name,
    start_offset,
    duration,
    task_id,
    resource_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c603'),
    'T2->R3',
    1,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b602'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a603')
);

INSERT IGNORE INTO resource_requests (
    id,
    name,
    start_offset,
    duration,
    task_id,
    resource_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c604'),
    'T3->R1',
    1,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b603'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a601')
);

INSERT IGNORE INTO resource_requests (
    id,
    name,
    start_offset,
    duration,
    task_id,
    resource_id
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c605'),
    'T3->R2',
    1,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b603'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a602')
);
INSERT IGNORE INTO tasks (
    id,
    name,
    arrival_time,
    duration,
    deadline,
    priority,
    scenario_id
)
WITH RECURSIVE task_numbers AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1
    FROM task_numbers
    WHERE n < 30
)
SELECT
    UUID_TO_BIN(UUID()),
    CONCAT('T', n),
    1,
    10,
    301,
    1,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000006')
FROM task_numbers;



INSERT IGNORE INTO scenarios (
    id,
    name
)
VALUES (
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008'),
    'Priority Inversion - Highest Task Blocked by the Lowest'
);

INSERT IGNORE INTO resources (
    id,
    name,
    priority_ceiling,
    scenario_id
)
VALUES
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a801'),
    'R1',
    5,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a802'),
    'R2',
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a803'),
    'R3',
    3,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a804'),
    'R4',
    4,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a805'),
    'R5',
    4,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a806'),
    'R6',
    5,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
);

INSERT IGNORE INTO tasks (
    id,
    name,
    arrival_time,
    duration,
    deadline,
    priority,
    scenario_id
)
VALUES
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b801'),
    'T1',
    0,
    8,
    35,
    1,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b802'),
    'T2',
    10,
    6,
    30,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b803'),
    'T3',
    11,
    6,
    25,
    3,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b804'),
    'T4',
    12,
    5,
    20,
    4,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b805'),
    'T5',
    2,
    5,
    8,
    5,
    UUID_TO_BIN('00000000-0000-0000-0000-000000000008')
);

INSERT IGNORE INTO resource_requests (
    id,
    name,
    start_offset,
    duration,
    task_id,
    resource_id
)
VALUES
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c801'),
    'T1->R1',
    1,
    3,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b801'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a801')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c802'),
    'T5->R1',
    0,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b805'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a801')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c803'),
    'T5->R6',
    2,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b805'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a806')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c804'),
    'T2->R2',
    0,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b802'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a802')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c805'),
    'T3->R3',
    0,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b803'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a803')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c806'),
    'T4->R4',
    0,
    5,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b804'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a804')
),
(
    UUID_TO_BIN('00000000-0000-0000-0000-00000000c807'),
    'T4->R5',
    2,
    2,
    UUID_TO_BIN('00000000-0000-0000-0000-00000000b804'),
    UUID_TO_BIN('00000000-0000-0000-0000-00000000a805')
);
