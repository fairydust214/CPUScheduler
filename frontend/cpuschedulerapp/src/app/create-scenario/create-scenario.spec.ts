import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateScenario } from './create-scenario';

describe('CreateScenario', () => {
  let component: CreateScenario;
  let fixture: ComponentFixture<CreateScenario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateScenario],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateScenario);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
